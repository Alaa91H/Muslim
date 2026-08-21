package org.muslim.app.feature.quran.data

/**
 * Pure decision logic for auto-resuming recitation after an audio-focus loss.
 *
 * Android's [android.media.AudioManager] delivers focus changes to
 * [RecitationPlaybackService]; this tiny state machine decides *whether* to
 * resume once focus comes back, so the behavior can be unit-tested on the JVM:
 *
 *  - A transient loss (navigation voice, another app, an alert) while the
 *    recitation was actually playing pauses it and marks it for auto-resume.
 *  - A transient loss while the user had already paused it does **not** arm
 *    auto-resume (respecting the user's choice).
 *  - A permanent loss (phone call, another app took over) never auto-resumes.
 *  - [onGain] consumes the flag once, so a second focus gain without a fresh
 *    loss cannot double-resume.
 */
class RecitationFocusPolicy {

    private var resumeAfterTransientLoss = false

    /** Focus lost temporarily. [wasPlaying] = the player state at that moment. */
    fun onTransientLoss(wasPlaying: Boolean) {
        resumeAfterTransientLoss = wasPlaying
    }

    /** Focus lost permanently (call / another app took over): never auto-resume. */
    fun onPermanentLoss() {
        resumeAfterTransientLoss = false
    }

    /**
     * Focus regained. Returns `true` exactly once when the recitation should
     * auto-resume (a playing recitation lost focus transiently and the user
     * hadn't paused it manually in the meantime).
     */
    fun onGain(): Boolean {
        val resume = resumeAfterTransientLoss
        resumeAfterTransientLoss = false
        return resume
    }
}
