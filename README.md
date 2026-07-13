# Echo Speaks Sound Sequencer

A Hubitat parent/child app for building ordered sound and speech sequences that play through devices managed by [Echo Speaks](https://github.com/tonesto7/echo-speaks).

The app sends SSML directly to the Echo Speaks device `speak()` command, allowing sequences that mix:

- Alexa Sound Library effects
- Custom `soundbank://` URIs
- Directly accessible HTTPS audio files
- Spoken messages
- Multiple supported Polly voices
- Multiple Echo devices

Echo Speaks is required. This app does not communicate with Alexa or Amazon directly.

## Features

- Parent app manages multiple independent sequences
- Each sequence is created as a child app
- Trigger playback from any physical or virtual Hubitat switch
- Add any number of sound and message steps
- Play steps in the exact displayed order
- Move steps up or down
- Remove individual steps
- Use different voices for different message steps
- Use the sequence name as a spoken message
- Select from built-in sound presets
- Use custom Amazon Sound Library URIs
- Use compatible externally hosted HTTPS audio files
- Target one or more Echo Speaks devices
- Optionally set a temporary playback volume
- Restore each Echo device to its previous volume afterward
- Optional debug logging

## Requirements

- Hubitat Elevation
- Echo Speaks installed and working
- At least one Echo Speaks device
- A physical or virtual Hubitat switch for each sequence you want to trigger

Before installing this app, verify that the `speak` command works directly from the Echo Speaks device page.

## Repository Layout

```text
echo-speaks-sound-sequencer/
├── apps/
│   ├── echo-speaks-sound-sequencer-parent.groovy
│   └── echo-speaks-sound-sequencer-child.groovy
├── resources/
└── README.md
```

Suggested app names:

```text
Parent app:
Echo Speaks Sound Sequencer

Child app:
Echo Speaks Sound - Sequence
```

## Installation

### Manual installation

1. Open Hubitat.
2. Go to **Developer Tools → Apps Code**.
3. Click **New App**.
4. Paste the parent app code from
   `apps/echo-speaks-sound-sequencer-parent.groovy`.
5. Click **Save**.
6. Create a second new app.
7. Paste the child app code from
   `apps/echo-speaks-sound-sequencer-child.groovy`.
8. Click **Save**.
9. Go to **Apps → Add User App**.
10. Install **Echo Speaks Sound Sequencer**.
11. Open the parent app and select **Add a new Echo sequence**.

Save the parent code before saving the child code.

### Import from GitHub

Use this raw URL for the parent app:

```text
https://raw.githubusercontent.com/smcallah/echo-speaks-sound-sequencer/main/apps/echo-speaks-sound-sequencer-parent.groovy
```

Use this raw URL for the child app:

```text
https://raw.githubusercontent.com/smcallah/echo-speaks-sound-sequencer/main/apps/echo-speaks-sound-sequencer-child.groovy
```

In Hubitat:

1. Go to **Developer Tools → Apps Code**.
2. Create a new app.
3. Open the editor menu.
4. Select **Import**.
5. Paste the raw GitHub URL.
6. Confirm the import.
7. Click **Save**.

## Creating a Sequence

1. Open **Apps → Echo Speaks Sound Sequencer**.
2. Select **Add a new Echo sequence**.
3. Enter a sequence name.
4. Select a physical or virtual trigger switch.
5. Select one or more Echo Speaks devices.
6. Add message and sound steps.
7. Arrange them in the desired order.
8. Configure the optional temporary playback volume.
9. Click **Done**.

When the selected switch reports `on`, the sequence will play. The app does
not turn the trigger switch off; configure automatic shutoff on the virtual
switch itself if needed.

## Playback Steps

### Message Step

A message step can:

- Speak custom text
- Use the sequence name as the message
- Use the default Alexa voice
- Use any Amazon Polly voice supported by Alexa SSML

Example:

```text
Message: Dinner is ready!
Voice: Matthew
```

The app builds the SSML internally:

```xml
<voice name="Matthew">Dinner is ready!</voice>
```

Voices are grouped by language and locale. When a locale-specific voice is
selected, the app adds the matching `<lang>` element automatically. Message
text should be written in the language associated with the selected voice.

### Sound Step

A sound step can use:

- A preset sound from the dropdown
- A custom Amazon Sound Library URI
- A directly accessible HTTPS audio file

Example Amazon Sound Library URI:

```text
soundbank://soundlibrary/animals/amzn_sfx_monkeys_chatter_01
```

Example HTTPS audio URL:

```text
https://example.com/audio/announcement.mp3
```

The app builds the SSML internally:

```xml
<audio src="soundbank://soundlibrary/animals/amzn_sfx_monkeys_chatter_01"/>
```

## Example Sequences

### Dinner Announcement

```text
1. Sound: Game show intro
2. Message: Dinner is ready!
3. Sound: Game show positive response
```

### Sound-Only Sequence

```text
1. Sound: Monkey calls
2. Sound: Monkey chatter
3. Sound: Crowd applause
```

### Mixed Custom Audio Sequence

```text
1. Sound: Custom HTTPS audio
2. Message: The garage door is still open.
3. Sound: Science-fiction alarm
```

## Volume Handling

Temporary playback volume is optional.

When enabled, the app:

1. Reads the current volume from each selected Echo device.
2. Sets the configured temporary playback volume.
3. Plays the sequence.
4. Waits for the configured restore delay.
5. Restores each Echo to its previous volume.

Set the restore delay long enough for the complete sequence to finish. If it is too short, the volume may change while the sequence is still playing.

For a multi-part native Echo Speaks sequence, the app places the wait and
volume-restore actions at the end of the same sequence. In that case, the
restore delay begins after the final speech or sound command finishes.

## Long Sequences

Echo Speaks rejects direct SSML commands above an observed limit of
approximately 449 characters. For long sequences, the app keeps each native
Echo Speaks sequence item at 390 characters or fewer. It
splits long messages at sentence boundaries when possible, then falls back to
word boundaries. It rebuilds complete voice and locale tags around each part
and groups sound and message steps into ordered commands without splitting an
audio tag. Multi-part sequences use Echo Speaks' native
`executeSequenceCommand()` queue when the selected device supports it.

When debug logging is enabled, the app reports the total generated SSML
length, the number of commands, and the length and contents of each command.

## Why This App Exists

Hubitat Rule Machine may strip or alter SSML tags entered into text fields. Echo Speaks Actions also does not always pass every SSML element through the same way as the direct Echo Speaks device `speak()` command.

This app avoids those paths by:

1. Building SSML inside the Groovy app.
2. Calling the Echo Speaks device `speak(String)` command directly.

That allows `<voice>` and `<audio>` elements to reach the working Echo Speaks device command without being rewritten by Rule Machine or the Echo Speaks Actions response editor.

## Important Notes

### Echo Speaks is required

This app depends on Echo Speaks devices and commands. It is not a replacement for Echo Speaks.

### HTTPS audio requirements

External audio must be directly accessible to Amazon without authentication, redirects that Alexa cannot follow, or locally trusted certificates.

The file must also use a format and encoding Alexa accepts. A URL working in a browser does not guarantee Alexa will play it.

### Amazon Sound Library URIs

Amazon Sound Library identifiers must be exact. An invalid or retired URI may be skipped while other steps continue to play.

### Debug logging

Enable debug logging in a sequence child app to view:

- The generated SSML
- Selected Echo devices
- Temporary volume changes
- Scheduled volume restoration

Avoid posting full logs publicly without reviewing them. Echo Speaks logs may contain device identifiers or account-related metadata.

## Renaming the Apps

Do not rename the parent or child `definition(name: ...)` values while installed child sequences still exist.

Hubitat treats the exact combination of namespace and app name as the app type identity. Renaming code does not migrate existing installed app instances.

Safe rename procedure:

1. Remove all existing child sequences.
2. Remove the installed parent app.
3. Rename both app definitions.
4. Update the child app's `parent:` value.
5. Update the parent app's `appName:` value.
6. Save the parent code.
7. Save the child code.
8. Reinstall the parent.
9. Recreate the child sequences.

An old child may otherwise remain subscribed to its trigger switch even when it no longer appears normally in the Apps list.

If that happens:

1. Open the trigger switch device page.
2. Find the old app under **In use by**.
3. Open the app from that list.
4. Remove it.

## Troubleshooting

### The sequence does not run

Check:

- The trigger switch is selected
- The switch is turning on
- At least one Echo Speaks device is selected
- At least one valid message or sound step exists
- Echo Speaks is authenticated and working
- The direct `speak` command works from the Echo device page

### Speech works but audio does not

Test the exact audio URI from the Echo Speaks device page using the `speak` command.

If it fails there, the URI or audio format is the problem rather than this app.

### Audio works from the device page but not Rule Machine

That is the problem this app is designed to bypass. Use the sequence app instead of passing raw SSML through Rule Machine.

### Duplicate playback

More than one installed app may be subscribed to the same trigger switch.

Open the trigger switch device page and inspect **In use by**. Remove obsolete or orphaned app instances.

### Volume does not restore correctly

- Increase the restore delay
- Confirm the Echo Speaks device reports its current volume
- Enable debug logging
- Check for volume-related errors in Hubitat logs

## Updating

After changing the code in GitHub:

1. Open the matching app under **Apps Code**.
2. Use **Import** to reload the raw GitHub URL.
3. Review the changes.
4. Click **Save**.
5. Open each installed child sequence and click **Done** if the update changes subscriptions or settings.

## License

Copyright 2026 Steven Callahan.

Licensed under the [Apache License 2.0](LICENSE).

## Versioning

Releases use `YY.Q.release`, where `YY` is the two-digit year, `Q` is the
calendar quarter, and `release` starts at 1 each quarter. The current version
is `26.3.1`.

## Credits

- Echo Speaks by Tony Fleisher / tonesto7
- Amazon Alexa Sound Library
- Hubitat Elevation
