package com.ninegag.moves.kmp

object Constants {
    val APP_IMAGE = "https://file-9gag-lol.9cache.com/file/zzooKna0Az/logo_landscape_white_bg.png"
    object RemoteConfigKeys {
        val DAILY_TARGET = "daily_target"
        val DAILT_TARGET_TICKET = "daily_target_ticket"
        val MIN_STEP_TARGET = "min_step_target"
    }

    object RemoteConfigDefaults {
        val DEFAULT_TARGET_TICKET = """
            [
              {
                "steps_min": 6000,
                "steps_max": 6999,
                "tickets": 1
              },
              {
                "steps_min": 7000,
                "steps_max": 7999,
                "tickets": 2
              },
              {
                "steps_min": 8000,
                "steps_max": 8999,
                "tickets": 3
              },
              {
                "steps_min": 9000,
                "steps_max": 9999,
                "tickets": 4
              },
              {
                "steps_min": 10000,
                "steps_max": 2147483647,
                "tickets": 5
              }
            ]
        """.trimIndent()
    }

    object Firestore {
        object Collections {
            const val USER = "users"
            const val STEPS = "steps"

            const val DAILY_STEPS = "daily_steps"
            const val WEEKLY_STEPS = "weekly_steps"
            const val MONTHLY_STEPS = "monthly_steps"
        }

        object CollectionFields {
            val USERNAME = "username"
            val EMAIL = "email"
            val AVATAR_URL = "avatarUrl"
            val STEPS = "steps"
        }
    }
}