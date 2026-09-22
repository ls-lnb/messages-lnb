package org.fossify.messages.activities

import android.os.Bundle
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.onTextChangeListener
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.messages.R
import org.fossify.messages.adapters.SenderPickerAdapter
import org.fossify.messages.databinding.ActivitySenderPickerBinding
import org.fossify.messages.dialogs.GroupSendersDialog
import org.fossify.messages.extensions.config
import org.fossify.messages.extensions.conversationsDB
import org.fossify.messages.extensions.getShortCodeSenderAddresses
import org.fossify.messages.extensions.saveSenderGroupFromSelection
import org.fossify.messages.extensions.similarShortCodeKey
import org.fossify.messages.helpers.PRESELECTED_SENDER_ADDRESSES
import org.fossify.messages.helpers.SUGGEST_SIMILAR_SENDERS
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.models.ShortCodeSender

class SenderPickerActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySenderPickerBinding::inflate)
    private val selectedAddresses = HashSet<String>()
    private var allSenders = ArrayList<ShortCodeSender>()
    private var groupTitles = emptyMap<String, String>()
    private var adapter: SenderPickerAdapter? = null
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        setupEdgeToEdge(padBottomImeAndSystem = listOf(binding.senderPickerList))
        setupMaterialScrollListener(
            scrollingView = binding.senderPickerList,
            topAppBar = binding.senderPickerAppbar
        )

        intent.getStringArrayListExtra(PRESELECTED_SENDER_ADDRESSES)
            ?.map { it.uppercase() }
            ?.let { selectedAddresses.addAll(it) }

        binding.senderPickerSearch.onTextChangeListener { text ->
            currentQuery = text
            showFilteredSenders()
        }

        updateSelectedCount()
        loadSenders()
    }

    override fun onResume() {
        super.onResume()
        setupTopAppBar(binding.senderPickerAppbar, NavigationIcon.Arrow)
        binding.senderPickerProgress.setIndicatorColor(getProperPrimaryColor())
    }

    private fun setupOptionsMenu() {
        binding.senderPickerToolbar.inflateMenu(R.menu.menu_sender_picker)
        binding.senderPickerToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.confirm_group_senders -> {
                    confirmSelection()
                    true
                }

                else -> false
            }
        }
    }

    private fun loadSenders() {
        binding.senderPickerProgress.show()
        ensureBackgroundThread {
            val snippets = HashMap<String, String>()
            try {
                (conversationsDB.getNonArchived() + conversationsDB.getAllArchived()).forEach { conversation ->
                    snippets[conversation.phoneNumber.uppercase()] = conversation.snippet
                }
            } catch (_: Exception) {
            }

            val senders = getShortCodeSenderAddresses()
                .map { address ->
                    ShortCodeSender(
                        address = address,
                        snippet = snippets[address.uppercase()].orEmpty()
                    )
                }
                .sortedWith(
                    compareBy<ShortCodeSender> {
                        it.address.similarShortCodeKey() ?: it.address.uppercase()
                    }.thenBy { it.address.uppercase() }
                )

            val titles = HashMap<String, String>()
            config.senderGroups.forEach { group ->
                group.addresses.forEach { address ->
                    titles[address.uppercase()] = group.title
                }
            }

            val suggestSimilar = intent.getBooleanExtra(SUGGEST_SIMILAR_SENDERS, false)
            if (suggestSimilar) {
                val seed = selectedAddresses.firstOrNull()
                    ?: senders.firstOrNull()?.address?.uppercase()
                val key = seed?.similarShortCodeKey()
                if (key != null) {
                    senders
                        .filter { it.address.similarShortCodeKey() == key }
                        .forEach { selectedAddresses.add(it.address.uppercase()) }
                }
            }

            runOnUiThread {
                binding.senderPickerProgress.hide()
                allSenders = ArrayList(senders)
                groupTitles = titles
                if (suggestSimilar && selectedAddresses.size < 2) {
                    toast(R.string.no_similar_senders)
                }
                setupAdapter()
                showFilteredSenders()
                updateSelectedCount()
            }
        }
    }

    private fun setupAdapter() {
        adapter = SenderPickerAdapter(
            activity = this,
            selectedAddresses = selectedAddresses,
            groupTitles = groupTitles,
            onSelectionChanged = { updateSelectedCount() }
        )
        binding.senderPickerList.adapter = adapter
    }

    private fun showFilteredSenders() {
        val query = currentQuery.trim()
        val filtered = if (query.isEmpty()) {
            allSenders
        } else {
            val needle = query.uppercase()
            ArrayList(
                allSenders.filter {
                    it.address.uppercase().contains(needle) ||
                        it.snippet.uppercase().contains(needle) ||
                        groupTitles[it.address.uppercase()].orEmpty().uppercase().contains(needle)
                }
            )
        }
        adapter?.senders = filtered
        binding.senderPickerPlaceholder.beVisibleIf(filtered.isEmpty())
        binding.senderPickerList.beVisibleIf(filtered.isNotEmpty())
        if (allSenders.isEmpty()) {
            binding.senderPickerPlaceholder.text = getString(R.string.no_short_code_senders)
        } else {
            binding.senderPickerPlaceholder.text = getString(org.fossify.commons.R.string.no_items_found)
        }
    }

    private fun updateSelectedCount() {
        binding.senderPickerSelectedCount.text = getString(
            R.string.selected_senders_count,
            selectedAddresses.size
        )
    }

    private fun confirmSelection() {
        hideKeyboard()
        val selectedSenders = allSenders.filter {
            selectedAddresses.contains(it.address.uppercase())
        }
        if (selectedSenders.size < 2) {
            toast(R.string.select_at_least_two_senders)
            return
        }

        val overlappingGroups = selectedSenders.mapNotNull {
            config.findSenderGroupByAddress(it.address)
        }.distinctBy { it.id }
        val similarKey = selectedSenders.first().address.similarShortCodeKey()
        val prefilledName = overlappingGroups.singleOrNull()?.title
            ?: similarKey
            ?: selectedSenders.first().address

        GroupSendersDialog(this, prefilledName) { name ->
            binding.senderPickerProgress.show()
            ensureBackgroundThread {
                saveSenderGroupFromSelection(selectedSenders.map { it.address }, name)
                runOnUiThread {
                    refreshConversations(cacheOnly = true)
                    finish()
                }
            }
        }
    }
}
