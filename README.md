# Messages — a Fossify Messages fork

<img alt="Logo" src="graphics/icon.webp" width="120" />

This is a fork of [Fossify Messages](https://github.com/FossifyOrg/Messages), the privacy-focused, open-source SMS/MMS app: no ads, no internet permission for SMS, material design with dark theme, message blocking, SMS/MMS backup and restore, and efficient search. For everything about the base app, see the [Fossify Messages README](https://github.com/FossifyOrg/Messages#readme).

## What this fork adds

All of the base app's features, plus **sender grouping** — keeping short-code SMS clutter (banks, delivery services, promotions, especially Indian short codes) organized:

- **Automatic sender grouping** — short-code threads from the same sender are merged into a single conversation instead of spamming the conversation list.
- **Manual grouping** — group (and ungroup) any senders yourself, with confirmation before splitting a group apart.
- **Group icons & sync progress** — grouped conversations show a group icon, with progress shown while grouping syncs.
- **Searchable sender picker with similar-sender suggestions** — a picker that searches your senders and suggests similar-looking ones when building a group.
- **Search inside grouped conversations** — full-text search across all threads in a group, with match count and navigation between matches.
- **Sender codes in grouped threads** — received messages in a grouped thread display the sender's phone number so you can tell real senders apart.
- **Settings export/import** — back up and restore all settings, including your sender groups, as a portable JSON file.
- **CI releases** — a universal APK is built and released automatically on every push to `main`.

<div align="center">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1_en-US.png" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2_en-US.png" width="30%">
<img alt="App image" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3_en-US.png" width="30%">
</div>
