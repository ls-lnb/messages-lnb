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
import org.fossify.messages.extensions.canGroupSenders
import org.fossify.messages.extensions.config
import org.fossify.messages.extensions.getConversations
import org.fossify.messages.extensions.saveSenderGroupFromSelection
import org.fossify.messages.extensions.similarShortCodeKey
import org.fossify.messages.helpers.PRESELECTED_SENDER_ADDRESSES
import org.fossify.messages.helpers.SUGGEST_SIMILAR_SENDERS
import org.fossify.messages.helpers.refreshConversations
import org.fossify.messages.helpers.requestTelephonySyncProgress
import org.fossify.messages.models.Conversation

class SenderPickerActivity : SimpleActivity() {
    private val binding by viewBinding(ActivitySenderPickerBinding::inflate)
    private val selectedAddresses = HashSet<String>()
    private var allSenders = ArrayList<Conversation>()
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
            val conversations = getConversations(applySenderGroups = false)
                .filter { it.canGroupSenders() }
                .sortedWith(
                    compareBy<Conversation> {
                        it.phoneNumber.similarShortCodeKey() ?: it.phoneNumber.uppercase()
                    }.thenBy { it.phoneNumber.uppercase() }
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
                    ?: conversations.firstOrNull()?.phoneNumber?.uppercase()
                val key = seed?.similarShortCodeKey()
                if (key != null) {
                    conversations
                        .filter { it.phoneNumber.similarShortCodeKey() == key }
                        .forEach { selectedAddresses.add(it.phoneNumber.uppercase()) }
                }
            }

            runOnUiThread {
                binding.senderPickerProgress.hide()
                allSenders = ArrayList(conversations)
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
                    it.phoneNumber.uppercase().contains(needle) ||
                        it.title.uppercase().contains(needle) ||
                        it.snippet.uppercase().contains(needle) ||
                        groupTitles[it.phoneNumber.uppercase()].orEmpty().uppercase().contains(needle)
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
            selectedAddresses.contains(it.phoneNumber.uppercase())
        }
        if (selectedSenders.size < 2) {
            toast(R.string.select_at_least_two_senders)
            return
        }

        val overlappingGroups = selectedSenders.mapNotNull {
            config.findSenderGroupByAddress(it.phoneNumber)
        }.distinctBy { it.id }
        val similarKey = selectedSenders.first().phoneNumber.similarShortCodeKey()
        val prefilledName = overlappingGroups.singleOrNull()?.title
            ?: similarKey
            ?: selectedSenders.maxByOrNull { it.date }?.title.orEmpty()

        GroupSendersDialog(this, prefilledName) { name ->
            requestTelephonySyncProgress()
            ensureBackgroundThread {
                saveSenderGroupFromSelection(selectedSenders, name)
                runOnUiThread {
                    refreshConversations()
                    finish()
                }
            }
        }
    }
}
