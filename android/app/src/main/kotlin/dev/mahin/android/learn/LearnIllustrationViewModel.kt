package dev.mahin.android.learn

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.media.illustration.MahinIllustrationSource
import javax.inject.Inject

@HiltViewModel
class LearnIllustrationViewModel
    @Inject
    constructor(
        val illustrationSource: MahinIllustrationSource,
    ) : ViewModel()
