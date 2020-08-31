_This release is part of the source code release as outlined in [this announcement](https://www.spigotmc.org/threads/deathmessagesprime.48322/page-53#post-3933244)._

# DMPMonitorCompat for DeathMessagesPrime

This add-on is designed for DeathMessagesPrime and some (chiefly chat) plugins that check the death message from PlayerDeathEvent using the MONITOR priority. While an accepted (and recommended) practice, any plugins using this approach will not be compatible with DeathMessagesPrime by default for technical reasons.

List of supported plugins
The plugin name to use under the plugins section is in quotes.

* DiscordSRV: `DiscordSRV`

Technical details
This plugin effectively creates a mock PlayerDeathEvent that will be sent to the PlayerDeathEvent listeners of the configured plugins. The mock event will have the correct player, drop, old XP and death message data, but all new XP data will be set to -1 (this way it is possible to tell the real and mock events apart).

The mock events may otherwise behave like real death events if not handled properly, which means DMPMonitorCompat is not necessarily directly compatible with these plugins either. The approach DMPMonitorCompat assumes is that the other plugin ignores death events with empty death messages, allowing the mock event to take its place.

Note that the real event will contain the default death message when DeathMessagesPrime is not installed (naturally), while if it is, the real event on MONITOR will have an empty death message.
