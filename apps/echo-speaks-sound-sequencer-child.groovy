definition(
    name: "Echo Speaks Sound - Sequence",
    namespace: "smcallah",
    author: "Steven Callahan",
    description: "An ordered sequence of Echo Speaks messages and sounds.",
    category: "Convenience",
    parent: "smcallah:Echo Speaks Sound Sequencer",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

/*
 * Main configuration page
 */

Map mainPage() {
    initializeStepState()

    dynamicPage(
        name: "mainPage",
        title: "Echo Speaks Sound - Sequence",
        install: true,
        uninstall: true
    ) {
        section("Sequence") {
            input(
                name: "sequenceName",
                type: "text",
                title: "Sequence name",
                description: "Example: Dinner is Ready",
                required: true,
                submitOnChange: true
            )
        }

        section("Trigger") {
            input(
                name: "triggerSwitch",
                type: "capability.switch",
                title: "Trigger switch",
                description:
                    "Select any physical or virtual Hubitat switch. " +
                    "The sequence plays when it reports on.",
                required: true,
                multiple: false
            )
        }

        section("Echo devices") {
            input(
                name: "echoDevices",
                type: "capability.speechSynthesis",
                title: "Echo Speaks devices",
                description: "Select one or more Echo devices.",
                required: true,
                multiple: true
            )
        }

        renderSequenceSteps()

        section("Add playback step") {
            input(
                name: "add_message_step",
                type: "button",
                title: "＋ Add message"
            )

            input(
                name: "add_sound_step",
                type: "button",
                title: "＋ Add sound"
            )

            if (getSequenceSteps().isEmpty()) {
                paragraph(
                    "The sequence is empty. Add at least one message " +
                    "or sound."
                )
            }
        }

        section("Volume") {
            input(
                name: "changeVolume",
                type: "bool",
                title: "Set a temporary playback volume",
                defaultValue: false,
                submitOnChange: true
            )

            if (changeVolume == true) {
                input(
                    name: "playbackVolume",
                    type: "number",
                    title: "Temporary playback volume",
                    description:
                        "Volume used while the sequence is playing.",
                    required: true,
                    defaultValue: 40,
                    range: "1..100"
                )

                input(
                    name: "restoreVolume",
                    type: "bool",
                    title: "Restore each Echo's previous volume",
                    defaultValue: true,
                    submitOnChange: true
                )

                if (restoreVolume == true) {
                    input(
                        name: "volumeRestoreDelay",
                        type: "number",
                        title: "Volume restore delay in seconds",
                        description:
                            "Set this long enough for the complete " +
                            "sequence to finish.",
                        required: true,
                        defaultValue: 8,
                        range: "1..300"
                    )
                }
            }
        }

        section("Logging") {
            input(
                name: "debugLogging",
                type: "bool",
                title: "Enable debug logging",
                defaultValue: false
            )
        }
    }
}

/*
 * Render all playback steps in their stored order.
 */

void renderSequenceSteps() {
    List<Map> steps = getSequenceSteps()

    if (steps.isEmpty()) {
        section("Playback sequence") {
            paragraph("No playback steps have been added.")
        }

        return
    }

    steps.eachWithIndex { Map step, Integer index ->
        String stepId = step.id.toString()
        String stepType = step.type.toString()
        Integer displayNumber = index + 1

        if (stepType == "message") {
            renderMessageStep(
                stepId,
                index,
                displayNumber,
                steps.size()
            )
        } else if (stepType == "sound") {
            renderSoundStep(
                stepId,
                index,
                displayNumber,
                steps.size()
            )
        }
    }
}

/*
 * Render one message step.
 */

void renderMessageStep(
    String stepId,
    Integer index,
    Integer displayNumber,
    Integer stepCount
) {
    String useNameSetting = "stepUseName_${stepId}"
    String textSetting = "stepText_${stepId}"
    String voiceSetting = "stepVoice_${stepId}"

    Boolean useSequenceName =
        settings[useNameSetting] == true

    section("${displayNumber}. Message") {
        input(
            name: useNameSetting,
            type: "bool",
            title: "Use the sequence name as this message",
            defaultValue: false,
            submitOnChange: true
        )

        if (!useSequenceName) {
            input(
                name: textSetting,
                type: "text",
                title: "Message to speak",
                description:
                    "Enter the exact words Alexa should speak.",
                required: true
            )
        }

        input(
            name: voiceSetting,
            type: "enum",
            title: "Voice",
            description:
                "Leave as Default Alexa voice for normal Alexa speech.",
            required: false,
            defaultValue: "default",
            options: voiceOptions()
        )

        renderStepControls(
            stepId,
            index,
            stepCount
        )
    }
}

/*
 * Render one sound step.
 */

void renderSoundStep(
    String stepId,
    Integer index,
    Integer displayNumber,
    Integer stepCount
) {
    String soundSetting = "stepSound_${stepId}"
    String customUriSetting = "stepCustomUri_${stepId}"

    String selectedSound =
        settings[soundSetting]?.toString()

    section("${displayNumber}. Sound") {
        input(
            name: soundSetting,
            type: "enum",
            title: "Sound",
            description:
                "Select a preset or choose a custom URI.",
            required: true,
            submitOnChange: true,
            options: soundOptions(),
            width: 5,
            newLineAfter: false
        )

        renderStepControls(
            stepId,
            index,
            stepCount,
            true
        )

        if (selectedSound == "custom") {
            input(
                name: customUriSetting,
                type: "text",
                title: "Custom audio URI",
                description:
                    "Use a soundbank:// URI or a directly accessible " +
                    "HTTPS audio URL.",
                required: true,
                width: 12,
                newLine: true
            )
        }
    }
}

/*
 * Render remove and ordering controls beneath a step.
 */

void renderStepControls(
    String stepId,
    Integer index,
    Integer stepCount,
    Boolean compactRow = false
) {
    Boolean hasMoveUp = index > 0
    Boolean hasMoveDown = index < stepCount - 1

    input(
        name: "remove_${stepId}",
        type: "button",
        title: "− Remove this step",
        width: compactRow ? 3 : 12,
        newLineAfter:
            compactRow ?
                (!hasMoveUp && !hasMoveDown) :
                true
    )

    if (hasMoveUp) {
        input(
            name: "move_up_${stepId}",
            type: "button",
            title: "↑ Move up",
            width: compactRow ? 2 : 12,
            newLineAfter:
                compactRow ? !hasMoveDown : true
        )
    }

    if (hasMoveDown) {
        input(
            name: "move_down_${stepId}",
            type: "button",
            title: "↓ Move down",
            width: compactRow ? 2 : 12,
            newLineAfter: true
        )
    }
}

/*
 * Handle dynamic-page buttons.
 */

void appButtonHandler(String buttonName) {
    initializeStepState()

    if (buttonName == "add_message_step") {
        addSequenceStep("message")
        return
    }

    if (buttonName == "add_sound_step") {
        addSequenceStep("sound")
        return
    }

    if (buttonName.startsWith("remove_")) {
        String stepId =
            buttonName.substring("remove_".length())

        removeSequenceStep(stepId)
        return
    }

    if (buttonName.startsWith("move_up_")) {
        String stepId =
            buttonName.substring("move_up_".length())

        moveSequenceStep(stepId, -1)
        return
    }

    if (buttonName.startsWith("move_down_")) {
        String stepId =
            buttonName.substring("move_down_".length())

        moveSequenceStep(stepId, 1)
    }
}

/*
 * Step-state management
 */

void initializeStepState() {
    if (!(atomicState.sequenceSteps instanceof List)) {
        atomicState.sequenceSteps = []
    }
}

List<Map> getSequenceSteps() {
    initializeStepState()

    List storedSteps =
        atomicState.sequenceSteps ?: []

    return storedSteps.collect { step ->
        [
            id: step.id.toString(),
            type: step.type.toString()
        ]
    }
}

void saveSequenceSteps(List<Map> steps) {
    atomicState.sequenceSteps = steps.collect { Map step ->
        [
            id: step.id.toString(),
            type: step.type.toString()
        ]
    }
}

void addSequenceStep(String stepType) {
    List<Map> steps = getSequenceSteps()

    String stepId = createStepId()

    steps.add(
        [
            id: stepId,
            type: stepType
        ]
    )

    saveSequenceSteps(steps)
}

void removeSequenceStep(String stepId) {
    List<Map> steps = getSequenceSteps()

    steps.removeAll { Map step ->
        step.id.toString() == stepId
    }

    saveSequenceSteps(steps)

    /*
     * Remove obsolete settings where supported. Failure here is harmless;
     * unused settings do not affect sequence playback.
     */
    removeStepSettings(stepId)
}

void moveSequenceStep(
    String stepId,
    Integer direction
) {
    List<Map> steps = getSequenceSteps()

    Integer currentIndex = steps.findIndexOf { Map step ->
        step.id.toString() == stepId
    }

    if (currentIndex < 0) {
        return
    }

    Integer destinationIndex =
        currentIndex + direction

    if (
        destinationIndex < 0 ||
        destinationIndex >= steps.size()
    ) {
        return
    }

    Map movingStep = steps.remove(currentIndex)

    steps.add(
        destinationIndex,
        movingStep
    )

    saveSequenceSteps(steps)
}

String createStepId() {
    Long timestamp = now()
    Integer randomPart =
        new Random().nextInt(9000) + 1000

    return "${timestamp}_${randomPart}"
}

void removeStepSettings(String stepId) {
    List<String> settingNames = [
        "stepUseName_${stepId}",
        "stepText_${stepId}",
        "stepVoice_${stepId}",
        "stepSound_${stepId}",
        "stepCustomUri_${stepId}"
    ]

    settingNames.each { String settingName ->
        try {
            app.removeSetting(settingName)
        } catch (Exception ignored) {
            /*
             * Some platform versions may retain obsolete dynamic settings.
             * They are ignored because the step no longer exists.
             */
        }
    }
}

/*
 * Lifecycle
 */

void installed() {
    initializeStepState()
    initializeVolumeRestoreState()
    updateAppLabel()
    initialize()
}

void updated() {
    unsubscribe()

    initializeStepState()
    initializeVolumeRestoreState()
    updateAppLabel()
    initialize()
}

void uninstalled() {
    unsubscribe()
    restorePendingVolumes(true)
    unschedule()
}

void initialize() {
    if (triggerSwitch) {
        subscribe(
            triggerSwitch,
            "switch.on",
            switchHandler
        )
    }
}

void updateAppLabel() {
    String newLabel = sequenceName?.trim()

    if (newLabel) {
        app.updateLabel(newLabel)
    }
}

/*
 * Trigger and playback
 */

void switchHandler(evt) {
    String ssml = buildSequenceSsml()

    if (!ssml) {
        log.error(
            "The sequence contains no playable content. " +
            "Add and configure at least one message or sound step."
        )

        return
    }

    if (debugLogging == true) {
        log.debug "Sending SSML: ${ssml}"
        log.debug(
            "Echo devices: " +
            "${echoDevices?.collect { it.displayName }}"
        )
    }

    echoDevices?.each { echoDevice ->
        sendToEcho(
            echoDevice,
            ssml
        )
    }

}

String buildSequenceSsml() {
    StringBuilder ssml =
        new StringBuilder()

    List<Map> steps = getSequenceSteps()

    steps.eachWithIndex { Map step, Integer index ->
        String stepId = step.id.toString()
        String stepType = step.type.toString()

        if (stepType == "message") {
            appendMessageStep(
                ssml,
                stepId,
                index
            )
        } else if (stepType == "sound") {
            appendSoundStep(
                ssml,
                stepId,
                index
            )
        }
    }

    String result = ssml.toString()

    return result?.trim() ? result : null
}

void appendMessageStep(
    StringBuilder ssml,
    String stepId,
    Integer index
) {
    Boolean useSequenceName =
        settings["stepUseName_${stepId}"] == true

    String message

    if (useSequenceName) {
        message = sequenceName?.trim()
    } else {
        message =
            settings["stepText_${stepId}"]?.toString()?.trim()
    }

    if (!message) {
        log.warn(
            "Skipping message step ${index + 1}: " +
            "no message was configured."
        )
        return
    }

    String voice =
        settings["stepVoice_${stepId}"]?.toString()

    String escapedMessage =
        escapeXmlText(message)

    if (voice && voice != "default") {
        ssml.append('<voice name="')
        ssml.append(
            escapeXmlAttribute(voice)
        )
        ssml.append('">')
        ssml.append(escapedMessage)
        ssml.append('</voice>')
    } else {
        ssml.append(escapedMessage)
    }
}

void appendSoundStep(
    StringBuilder ssml,
    String stepId,
    Integer index
) {
    String selection =
        settings["stepSound_${stepId}"]?.toString()

    String customUri =
        settings["stepCustomUri_${stepId}"]
            ?.toString()
            ?.trim()

    String uri = resolveSoundUri(
        selection,
        customUri
    )

    if (!uri) {
        log.warn(
            "Skipping sound step ${index + 1}: " +
            "no valid sound was configured."
        )
        return
    }

    ssml.append('<audio src="')
    ssml.append(
        escapeXmlAttribute(uri)
    )
    ssml.append('"/>')
}

/*
 * Echo device command and volume management
 */

void sendToEcho(
    echoDevice,
    String ssml
) {
    try {
        if (changeVolume == true) {
            Integer previousVolume =
                getOriginalVolume(echoDevice)

            if (
                restoreVolume == true &&
                previousVolume == null
            ) {
                log.warn(
                    "${echoDevice.displayName}: skipping temporary " +
                    "volume because its current volume could not " +
                    "be read for restoration"
                )
            } else {
                Integer temporaryVolume =
                    safeInteger(
                        playbackVolume,
                        40
                    )

                if (debugLogging == true) {
                    log.debug(
                        "${echoDevice.displayName}: changing volume " +
                        "from ${previousVolume} to ${temporaryVolume}"
                    )
                }

                echoDevice.setVolume(
                    temporaryVolume
                )

                if (restoreVolume == true) {
                    try {
                        scheduleVolumeRestore(
                            echoDevice,
                            previousVolume
                        )
                    } catch (Exception scheduleException) {
                        removePendingVolumeRestore(
                            echoDevice.id.toString()
                        )

                        echoDevice.setVolume(
                            previousVolume
                        )

                        throw scheduleException
                    }
                }

                /*
                 * Let the volume command get ahead of playback.
                 */
                pauseExecution(300)
            }
        }

        /*
         * Direct speak(String) is intentional. It preserves the SSML that
         * Rule Machine and Echo Speaks Actions otherwise rewrite.
         */
        echoDevice.speak(
            ssml as String
        )
    } catch (Exception ex) {
        log.error(
            "Unable to send sequence to " +
            "${echoDevice?.displayName}: " +
            "${ex.class.simpleName}: ${ex.message}"
        )
    }
}

void scheduleVolumeRestore(
    echoDevice,
    Integer previousVolume
) {
    initializeVolumeRestoreState()

    Integer delaySeconds =
        safeInteger(
            volumeRestoreDelay,
            8
        )

    Long restoreAt =
        now() + (delaySeconds * 1000L)

    String deviceId =
        echoDevice.id.toString()

    Map pendingRestores =
        atomicState.pendingVolumeRestores ?: [:]

    Map existingRestore =
        pendingRestores[deviceId] as Map

    Integer originalVolume =
        safeInteger(
            existingRestore?.volume,
            previousVolume
        )

    pendingRestores[deviceId] = [
        volume: originalVolume,
        restoreAt: restoreAt
    ]

    atomicState.pendingVolumeRestores =
        pendingRestores

    if (debugLogging == true) {
        log.debug(
            "${echoDevice.displayName}: scheduling volume " +
            "restore to ${originalVolume} in " +
            "${delaySeconds} seconds"
        )
    }

    scheduleNextVolumeRestore()
}

void initializeVolumeRestoreState() {
    if (!(atomicState.pendingVolumeRestores instanceof Map)) {
        atomicState.pendingVolumeRestores = [:]
    }
}

void removePendingVolumeRestore(String deviceId) {
    initializeVolumeRestoreState()

    Map pendingRestores =
        atomicState.pendingVolumeRestores ?: [:]

    pendingRestores.remove(deviceId)

    atomicState.pendingVolumeRestores =
        pendingRestores
}

Integer getOriginalVolume(echoDevice) {
    initializeVolumeRestoreState()

    String deviceId =
        echoDevice.id.toString()

    Map pendingRestore =
        atomicState.pendingVolumeRestores[deviceId] as Map

    if (pendingRestore?.volume != null) {
        return safeInteger(
            pendingRestore.volume,
            null
        )
    }

    return getDeviceVolume(echoDevice)
}

void scheduleNextVolumeRestore() {
    initializeVolumeRestoreState()

    List<Long> restoreTimes =
        atomicState.pendingVolumeRestores
            .values()
            .collect { Map pendingRestore ->
                pendingRestore.restoreAt as Long
            }

    if (restoreTimes.isEmpty()) {
        unschedule("restorePendingVolumes")
        return
    }

    Long delayMilliseconds =
        Math.max(
            1L,
            restoreTimes.min() - now()
        )

    runInMillis(
        delayMilliseconds,
        "restorePendingVolumes",
        [overwrite: true]
    )
}

void restorePendingVolumes(Boolean restoreAll = false) {
    initializeVolumeRestoreState()

    Long currentTime = now()
    Map pendingRestores =
        atomicState.pendingVolumeRestores ?: [:]
    Map remainingRestores = [:]

    pendingRestores.each {
        String deviceId,
        Map pendingRestore ->
        Long restoreAt =
            pendingRestore.restoreAt as Long

        if (!restoreAll && restoreAt > currentTime) {
            remainingRestores[deviceId] =
                pendingRestore
            return
        }

        def echoDevice = echoDevices?.find {
            it.id.toString() == deviceId
        }

        if (!echoDevice) {
            log.warn(
                "Unable to find Echo device ${deviceId} " +
                "for volume restoration"
            )
            return
        }

        Integer previousVolume =
            safeInteger(
                pendingRestore.volume,
                null
            )

        try {
            if (debugLogging == true) {
                log.debug(
                    "${echoDevice.displayName}: restoring volume " +
                    "to ${previousVolume}"
                )
            }

            echoDevice.setVolume(
                previousVolume
            )
        } catch (Exception ex) {
            log.warn(
                "Unable to restore volume on " +
                "${echoDevice.displayName}: " +
                "${ex.class.simpleName}: ${ex.message}"
            )

            pendingRestore.restoreAt =
                currentTime + 30000L
            remainingRestores[deviceId] =
                pendingRestore
        }
    }

    atomicState.pendingVolumeRestores =
        remainingRestores

    scheduleNextVolumeRestore()
}

Integer getDeviceVolume(echoDevice) {
    try {
        def currentVolume =
            echoDevice.currentValue("volume")

        if (currentVolume == null) {
            log.warn(
                "${echoDevice.displayName}: " +
                "current volume was unavailable"
            )
            return null
        }

        return currentVolume as Integer
    } catch (Exception ex) {
        log.warn(
            "Unable to read volume from " +
            "${echoDevice.displayName}: ${ex.message}"
        )
        return null
    }
}

/*
 * Preset voices and sounds
 */

Map voiceOptions() {
    return [
        "default": "Default Alexa voice",
        "Matthew": "Matthew",
        "Joanna": "Joanna",
        "Amy": "Amy",
        "Brian": "Brian",
        "Emma": "Emma",
        "Ivy": "Ivy",
        "Joey": "Joey",
        "Justin": "Justin",
        "Kendra": "Kendra",
        "Kimberly": "Kimberly",
        "Salli": "Salli"
    ]
}

Map soundOptions() {
    return [
        "custom":
            "Custom soundbank or HTTPS URI",

        "gameshow_intro":
            "Game show: Intro",

        "gameshow_positive":
            "Game show: Positive response 1",

        "gameshow_positive_2":
            "Game show: Positive response 2",

        "gameshow_negative":
            "Game show: Negative response",

        "gameshow_tally_positive":
            "Game show: Positive tally",

        "monkey_calls":
            "Monkey: Calls three times",

        "monkey_chimp":
            "Monkey: Chimp call",

        "monkeys_chatter":
            "Monkey: Group chatter",

        "bear_roar":
            "Animal: Bear roar",

        "cat_meow":
            "Animal: Cat meow",

        "dog_bark":
            "Animal: Dog bark",

        "lion_roar":
            "Animal: Lion roar",

        "wolf_howl":
            "Animal: Wolf howl",

        "rooster_crow":
            "Animal: Rooster crow",

        "crowd_applause":
            "Crowd: Applause",

        "crowd_cheer":
            "Crowd: Cheer",

        "crowd_boo":
            "Crowd: Boo",

        "laughter":
            "Human: Laughter",

        "doorbell":
            "Home: Doorbell chime",

        "boing":
            "Cartoon: Boing",

        "scifi_alarm":
            "Science fiction: Alarm",

        "scifi_sonar":
            "Science fiction: Sonar pings",

        "scifi_zap":
            "Science fiction: Electric zap"
    ]
}

String resolveSoundUri(
    String selection,
    String customUri
) {
    if (!selection) {
        return null
    }

    if (selection == "custom") {
        return normalizeCustomAudioInput(customUri)
    }

    Map<String, String> sounds =
        soundUriMap()

    String uri = sounds[selection]

    if (!uri) {
        log.warn(
            "Unknown sound selection: ${selection}"
        )
        return null
    }

    return uri
}

String normalizeCustomAudioInput(String customInput) {
    String value = customInput?.trim()

    if (!value) {
        return null
    }

    try {
        URI parsedUri = new URI(value)
        String scheme =
            parsedUri.scheme?.toLowerCase()

        Boolean validHttpsUri =
            scheme == "https" &&
            parsedUri.host

        Boolean validSoundbankUri =
            scheme == "soundbank" &&
            parsedUri.host &&
            parsedUri.path &&
            parsedUri.path != "/"

        if (validHttpsUri || validSoundbankUri) {
            return value
        }
    } catch (Exception ignored) {
        /* Invalid URI syntax is handled by the warning below. */
    }

    log.warn(
        "Custom audio must be an HTTPS URI or a soundbank:// URI."
    )

    return null
}

Map<String, String> soundUriMap() {
    return [
        "gameshow_intro":
            "soundbank://soundlibrary/ui/gameshow/" +
            "amzn_ui_sfx_gameshow_intro_01",

        "gameshow_positive":
            "soundbank://soundlibrary/ui/gameshow/" +
            "amzn_ui_sfx_gameshow_positive_response_01",

        "gameshow_positive_2":
            "soundbank://soundlibrary/ui/gameshow/" +
            "amzn_ui_sfx_gameshow_positive_response_02",

        "gameshow_negative":
            "soundbank://soundlibrary/ui/gameshow/" +
            "amzn_ui_sfx_gameshow_negative_response_01",

        "gameshow_tally_positive":
            "soundbank://soundlibrary/ui/gameshow/" +
            "amzn_ui_sfx_gameshow_tally_positive_01",

        "monkey_calls":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_monkey_calls_3x_01",

        "monkey_chimp":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_monkey_chimp_01",

        "monkeys_chatter":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_monkeys_chatter_01",

        "bear_roar":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_bear_groan_roar_01",

        "cat_meow":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_cat_meow_1x_01",

        "dog_bark":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_dog_med_bark_2x_01",

        "lion_roar":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_lion_roar_01",

        "wolf_howl":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_wolf_howl_01",

        "rooster_crow":
            "soundbank://soundlibrary/animals/" +
            "amzn_sfx_rooster_crow_01",

        "crowd_applause":
            "soundbank://soundlibrary/human/" +
            "amzn_sfx_crowd_applause_01",

        "crowd_cheer":
            "soundbank://soundlibrary/human/" +
            "amzn_sfx_crowd_cheer_med_01",

        "crowd_boo":
            "soundbank://soundlibrary/human/" +
            "amzn_sfx_crowd_boo_01",

        "laughter":
            "soundbank://soundlibrary/human/" +
            "amzn_sfx_laughter_01",

        "doorbell":
            "soundbank://soundlibrary/home/" +
            "amzn_sfx_doorbell_chime_01",

        "boing":
            "soundbank://soundlibrary/cartoon/" +
            "amzn_sfx_boing_long_1x_01",

        "scifi_alarm":
            "soundbank://soundlibrary/scifi/" +
            "amzn_sfx_scifi_alarm_01",

        "scifi_sonar":
            "soundbank://soundlibrary/scifi/" +
            "amzn_sfx_scifi_sonar_ping_3x_01",

        "scifi_zap":
            "soundbank://soundlibrary/scifi/" +
            "amzn_sfx_scifi_zap_electric_01"
    ]
}

/*
 * Utility functions
 */

Integer safeInteger(
    value,
    Integer fallback
) {
    try {
        if (value == null) {
            return fallback
        }

        return value as Integer
    } catch (Exception ignored) {
        return fallback
    }
}

String escapeXmlText(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

String escapeXmlAttribute(String value) {
    return escapeXmlText(value)
        .replace('"', "&quot;")
        .replace("'", "&apos;")
}
