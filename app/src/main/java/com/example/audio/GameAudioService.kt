package com.example.audio

/**
 * Abstraction for game audio so ViewModel and engine can be tested without static SoundManager.
 */
interface GameAudioService {
    var isSoundEnabled: Boolean

    fun playDrumBeat()
    fun playWarHorn()
    fun playSwordClash()
    fun playTriumphFanfare()
    fun playCoins()
    fun playGladiusClash()
}
