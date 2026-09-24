package dev.mahin.core.model

/**
 * Top-level reproductive mode. Transitions are explicit user actions.
 * Pregnancy is never inferred from lateness or symptoms.
 */
enum class ReproductiveMode {
    CYCLE_TRACKING,
    TRYING_TO_CONCEIVE,
    PREGNANT,
    POST_PREGNANCY_TRANSITION,
    TRACKING_PAUSED,
}
