package vad.dashing.tbox

/**
 * Donation channels shown in the About console («О лаунчере»).
 *
 * An empty list hides the section entirely. Fill in real payment URLs to enable it —
 * each entry renders as a QR (scannable from a phone) plus a copyable link.
 */
object DonationLinks {
    data class Entry(
        /** Channel name, e.g. "Boosty", "ЮMoney", "CloudTips". */
        @androidx.annotation.StringRes val titleResource: Int,
        /** Short hint shown under the title, e.g. "разовый перевод" / "подписка". */
        @androidx.annotation.StringRes val subtitleResource: Int,
        /** Payment URL opened on tap and encoded into the QR code. */
        val url: String,
    ) {
        val title: String get() = launcherText(titleResource)
        val subtitle: String get() = launcherText(subtitleResource)
    }

    val entries: List<Entry> = listOf(
        Entry(
            titleResource = R.string.label_t_bank_donations,
            subtitleResource = R.string.label_one_time_contribution_of_any_amount,
            url = "https://www.tbank.ru/cf/jle4TITNel",
        ),
    )

    /** Feedback channels (not donations) shown in the About console. */
    val feedbackEntries: List<Entry> = listOf(
        Entry(
            titleResource = R.string.label_telegram_bot,
            subtitleResource = R.string.label_feedback_and_setup_instructions,
            url = "https://t.me/Dashing_Club_Launcher_bot",
        ),
    )
}
