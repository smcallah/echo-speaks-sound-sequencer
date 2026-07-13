/**
 * Echo Speaks Sound Sequencer child app
 *
 * Copyright 2026 Steven Callahan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import groovy.transform.Field

@Field static final String APP_VERSION = "26.3.5"
@Field static final Integer DEBUG_LOG_SECONDS = 1800

definition(
    name: "Echo Speaks Sound - Sequence",
    namespace: "smcallah",
    author: "Steven Callahan",
    description: "An ordered sequence of Echo Speaks messages and sounds.",
    category: "Convenience",
    parent: "smcallah:Echo Speaks Sound Sequencer",
    iconUrl: "",
    iconX2Url: "",
    importUrl:
        "https://raw.githubusercontent.com/smcallah/" +
        "echo-speaks-sound-sequencer/main/apps/" +
        "echo-speaks-sound-sequencer-child.groovy",
    documentationLink:
        "https://github.com/smcallah/echo-speaks-sound-sequencer"
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
                type: "device.EchoSpeaksDevice",
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
                description: "Turns off automatically after 30 minutes.",
                defaultValue: false
            )

            paragraph("Version ${APP_VERSION}")
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
            submitOnChange: true,
            options: voiceOptions(),
            width: 5,
            newLineAfter: false
        )

        renderStepControls(
            stepId,
            index,
            stepCount,
            true
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
    initializeNativeVolumeRestoreState()
    updateAppLabel()
    initialize()
    configureDebugLogging()
}

void updated() {
    unsubscribe()

    initializeStepState()
    initializeVolumeRestoreState()
    initializeNativeVolumeRestoreState()
    updateAppLabel()
    initialize()
    configureDebugLogging()
}

void uninstalled() {
    unsubscribe()
    restorePendingVolumes(true)
    restoreNativePendingVolumes()
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
    List<String> ssmlChunks =
        buildSequenceSsmlChunks()

    if (ssmlChunks.isEmpty()) {
        log.error(
            "The sequence contains no playable content. " +
            "Add and configure at least one message or sound step."
        )

        return
    }

    if (debugLogging == true) {
        Integer totalLength =
            ssmlChunks.sum { String chunk ->
                chunk.length()
            } as Integer

        log.debug(
            "Sequence SSML length: ${totalLength} characters; " +
            "sending ${ssmlChunks.size()} command(s)"
        )

        ssmlChunks.eachWithIndex {
            String chunk,
            Integer chunkIndex ->
            log.debug(
                "SSML chunk ${chunkIndex + 1} " +
                "(${chunk.length()} characters; ‹ and › represent " +
                "angle brackets): ${formatSsmlForLog(chunk)}"
            )
        }

        log.debug(
            "Echo devices: " +
            "${echoDevices?.collect { it.displayName }}"
        )
    }

    echoDevices?.each { echoDevice ->
        sendToEcho(
            echoDevice,
            ssmlChunks
        )
    }

}

void configureDebugLogging() {
    unschedule("disableDebugLogging")

    if (debugLogging == true) {
        runIn(
            DEBUG_LOG_SECONDS,
            "disableDebugLogging",
            [overwrite: true]
        )
    }
}

void disableDebugLogging() {
    if (debugLogging == true) {
        log.debug "Disabling debug logging after 30 minutes"
        app.updateSetting(
            "debugLogging",
            [value: "false", type: "bool"]
        )
    }
}

List<String> buildSequenceSsmlChunks() {
    Integer maximumChunkLength = 390
    List<String> stepFragments = []
    List<Map> steps = getSequenceSteps()

    steps.eachWithIndex { Map step, Integer index ->
        String stepId = step.id.toString()
        String stepType = step.type.toString()

        if (stepType == "message") {
            stepFragments.addAll(
                buildMessageStepFragments(
                    stepId,
                    index,
                    maximumChunkLength
                )
            )
        } else if (stepType == "sound") {
            String soundFragment =
                buildSoundStepFragment(
                    stepId,
                    index
                )

            if (soundFragment) {
                stepFragments.add(soundFragment)
            }
        }
    }

    List<String> chunks = []
    StringBuilder currentChunk =
        new StringBuilder()

    stepFragments.each { String fragment ->
        if (fragment.length() > maximumChunkLength) {
            log.error(
                "Skipping a playback step because its generated SSML " +
                "is ${fragment.length()} characters, exceeding the " +
                "safe Echo Speaks limit of ${maximumChunkLength}."
            )
            return
        }

        if (
            currentChunk.length() > 0 &&
            currentChunk.length() + fragment.length() >
                maximumChunkLength
        ) {
            chunks.add(currentChunk.toString())
            currentChunk = new StringBuilder()
        }

        currentChunk.append(fragment)
    }

    if (currentChunk.length() > 0) {
        chunks.add(currentChunk.toString())
    }

    return chunks
}

List<String> buildMessageStepFragments(
    String stepId,
    Integer index,
    Integer maximumChunkLength
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
        return []
    }

    String voiceSelection =
        settings["stepVoice_${stepId}"]?.toString()

    if (debugLogging == true) {
        log.debug(
            "Message step ${index + 1} voice setting: " +
            "${voiceSelection ?: 'default'}"
        )
    }

    String prefix = ""
    String suffix = ""

    if (voiceSelection && voiceSelection != "default") {
        Map voiceDetails =
            parseVoiceSelection(voiceSelection)
        String voice =
            voiceDetails.name
        String locale =
            voiceDetails.locale

        prefix =
            '<voice name="' +
            escapeXmlAttribute(voice) +
            '">'
        suffix = '</voice>'

        if (locale) {
            prefix +=
                '<lang xml:lang="' +
                escapeXmlAttribute(locale) +
                '">'
            suffix =
                '</lang>' + suffix
        }
    }

    Integer availableTextLength =
        maximumChunkLength -
        prefix.length() -
        suffix.length()

    if (availableTextLength < 1) {
        log.error(
            "Skipping message step ${index + 1}: voice markup " +
            "exceeds the safe Echo Speaks command length."
        )
        return []
    }

    List<String> messageParts =
        splitMessageForSsml(
            message,
            availableTextLength
        )

    return messageParts.collect { String messagePart ->
        prefix +
            escapeXmlText(messagePart) +
            suffix
    }
}

List<String> splitMessageForSsml(
    String message,
    Integer maximumEscapedLength
) {
    List<String> parts = []
    String remaining = message.trim()

    while (remaining) {
        if (
            escapeXmlText(remaining).length() <=
                maximumEscapedLength
        ) {
            parts.add(remaining)
            break
        }

        Integer cutIndex = 0

        for (
            Integer index = 1;
            index <= remaining.length();
            index++
        ) {
            if (
                escapeXmlText(
                    remaining.substring(0, index)
                ).length() > maximumEscapedLength
            ) {
                break
            }

            cutIndex = index
        }

        if (cutIndex < 1) {
            log.error(
                "Unable to split a message within the safe Echo " +
                "Speaks command length."
            )
            return []
        }

        String candidate =
            remaining.substring(0, cutIndex)
        Integer sentenceIndex =
            findSentenceSplitIndex(candidate)

        if (sentenceIndex > 0) {
            cutIndex = sentenceIndex
        } else {
            Integer whitespaceIndex =
                candidate.lastIndexOf(' ')

            if (whitespaceIndex > 0) {
                cutIndex = whitespaceIndex
            }
        }

        String part =
            remaining.substring(0, cutIndex).trim()

        if (!part) {
            part =
                remaining.substring(0, cutIndex)
        }

        parts.add(part)
        remaining =
            remaining.substring(cutIndex).trim()
    }

    return parts
}

Integer findSentenceSplitIndex(String value) {
    List<String> sentenceEndings = [
        ". ",
        "! ",
        "? ",
        "。",
        "！",
        "？",
        "\n"
    ]
    Integer preferredIndex = -1

    sentenceEndings.each { String ending ->
        Integer endingIndex =
            value.lastIndexOf(ending)

        if (endingIndex >= 0) {
            Integer splitIndex =
                endingIndex +
                (ending == "\n" ? 0 : ending.trim().length())

            if (splitIndex > preferredIndex) {
                preferredIndex = splitIndex
            }
        }
    }

    return preferredIndex
}

String buildSoundStepFragment(
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
        return null
    }

    return '<audio src="' +
        escapeXmlAttribute(uri) +
        '"/>'
}

/*
 * Echo device command and volume management
 */

void sendToEcho(
    echoDevice,
    List<String> ssmlChunks
) {
    Integer previousVolume = null
    Integer temporaryVolume = null
    Boolean volumeWasChanged = false
    Boolean useNativeSequence = false

    try {
        useNativeSequence =
            canUseNativeSequence(
                ssmlChunks
            )

        if (changeVolume == true) {
            previousVolume =
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
                temporaryVolume =
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

                volumeWasChanged = true

                if (!useNativeSequence) {
                    echoDevice.setVolume(
                        temporaryVolume
                    )
                }

                if (
                    restoreVolume == true &&
                    !useNativeSequence
                ) {
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
                } else if (
                    restoreVolume == true &&
                    useNativeSequence
                ) {
                    trackNativeVolumeRestore(
                        echoDevice,
                        previousVolume
                    )
                }

                if (!useNativeSequence) {
                    /* Let the separate volume command get ahead of playback. */
                    pauseExecution(300)
                }
            }
        }

        /* Send SSML directly or through Echo Speaks' native sequence API. */
        sendSsmlChunks(
            echoDevice,
            ssmlChunks,
            useNativeSequence,
            (
                volumeWasChanged &&
                useNativeSequence
            ) ? temporaryVolume : null,
            (
                volumeWasChanged &&
                restoreVolume == true
            ) ? previousVolume : null
        )
    } catch (Exception ex) {
        if (
            useNativeSequence &&
            volumeWasChanged &&
            restoreVolume == true &&
            previousVolume != null
        ) {
            removeNativeVolumeRestore(
                echoDevice.id.toString()
            )

            try {
                echoDevice.setVolume(
                    previousVolume
                )
            } catch (Exception restoreException) {
                log.warn(
                    "Unable to roll back volume on " +
                    "${echoDevice?.displayName}: " +
                    "${restoreException.message}"
                )
            }
        }

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

void sendSsmlChunks(
    echoDevice,
    List<String> ssmlChunks,
    Boolean useNativeSequence,
    Integer nativePlaybackVolume,
    Integer nativeRestoreVolume
) {
    if (useNativeSequence) {
        List<String> sequenceItems = []

        if (nativePlaybackVolume != null) {
            sequenceItems.add(
                "volume::${nativePlaybackVolume}"
            )
        }

        sequenceItems.addAll(
            ssmlChunks.collect { String chunk ->
                "speak::${chunk}"
            }
        )

        if (nativeRestoreVolume != null) {
            Integer restoreDelay =
                safeInteger(
                    volumeRestoreDelay,
                    8
                )

            sequenceItems.add(
                "wait::${restoreDelay}"
            )
            sequenceItems.add(
                "volume::${nativeRestoreVolume}"
            )
        }

        String sequenceCommand =
            sequenceItems.join(",,")

        if (debugLogging == true) {
            log.debug(
                "${echoDevice.displayName}: sending chunks with " +
                "Echo Speaks executeSequenceCommand"
            )
        }

        echoDevice.executeSequenceCommand(
            sequenceCommand
        )
        return
    }

    if (ssmlChunks.size() == 1) {
        echoDevice.speak(
            ssmlChunks.first() as String,
            null,
            null,
            false
        )
        return
    }

    log.warn(
        "${echoDevice.displayName}: native Echo Speaks sequencing " +
        "was unavailable; sending chunks as individual commands"
    )

    ssmlChunks.each { String chunk ->
        echoDevice.speak(
            chunk as String,
            null,
            null,
            false
        )
    }
}

Boolean canUseNativeSequence(
    List<String> ssmlChunks
) {
    Boolean containsSequenceDelimiter =
        ssmlChunks.any { String chunk ->
            chunk.contains(",,") ||
                chunk.contains("::")
        }

    /*
     * The device selector only accepts Echo Speaks devices. Avoid hasCommand()
     * because Hubitat logs a MethodSelectionException while introspecting the
     * Echo Speaks driver's overloaded speak() methods.
     */
    return !containsSequenceDelimiter
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
    initializeNativeVolumeRestoreState()

    String deviceId =
        echoDevice.id.toString()

    Integer nativeOriginalVolume =
        safeInteger(
            atomicState.nativeVolumeRestores[deviceId],
            null
        )

    if (nativeOriginalVolume != null) {
        Integer currentVolume =
            getDeviceVolume(echoDevice)
        Integer temporaryVolume =
            safeInteger(
                playbackVolume,
                40
            )

        if (
            currentVolume != null &&
            currentVolume != temporaryVolume
        ) {
            removeNativeVolumeRestore(deviceId)
            return currentVolume
        }

        return nativeOriginalVolume
    }

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

void initializeNativeVolumeRestoreState() {
    if (!(atomicState.nativeVolumeRestores instanceof Map)) {
        atomicState.nativeVolumeRestores = [:]
    }
}

void trackNativeVolumeRestore(
    echoDevice,
    Integer previousVolume
) {
    initializeNativeVolumeRestoreState()

    Map nativeRestores =
        atomicState.nativeVolumeRestores ?: [:]
    String deviceId =
        echoDevice.id.toString()

    if (nativeRestores[deviceId] == null) {
        nativeRestores[deviceId] =
            previousVolume
        atomicState.nativeVolumeRestores =
            nativeRestores
    }
}

void removeNativeVolumeRestore(String deviceId) {
    initializeNativeVolumeRestoreState()

    Map nativeRestores =
        atomicState.nativeVolumeRestores ?: [:]

    nativeRestores.remove(deviceId)
    atomicState.nativeVolumeRestores =
        nativeRestores
}

void restoreNativePendingVolumes() {
    initializeNativeVolumeRestoreState()

    Map nativeRestores =
        atomicState.nativeVolumeRestores ?: [:]

    nativeRestores.each {
        String deviceId,
        previousVolume ->
        def echoDevice = echoDevices?.find {
            it.id.toString() == deviceId
        }

        if (echoDevice) {
            try {
                echoDevice.setVolume(
                    safeInteger(previousVolume, 40)
                )
            } catch (Exception ex) {
                log.warn(
                    "Unable to restore volume on " +
                    "${echoDevice.displayName} during uninstall: " +
                    "${ex.message}"
                )
            }
        }
    }

    atomicState.nativeVolumeRestores = [:]
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

Map parseVoiceSelection(String voiceSelection) {
    Map voiceAliases = [
        "AditiEnglish": [
            name: "Aditi",
            locale: "en-IN"
        ],
        "AditiHindi": [
            name: "Aditi",
            locale: "hi-IN"
        ]
    ]

    if (voiceAliases[voiceSelection]) {
        return voiceAliases[voiceSelection]
    }

    Integer safeSeparatorIndex =
        voiceSelection.indexOf('__')

    if (safeSeparatorIndex >= 0) {
        return [
            name: voiceSelection.substring(
                0,
                safeSeparatorIndex
            ),
            locale: voiceSelection.substring(
                safeSeparatorIndex + 2
            ).replace('_', '-')
        ]
    }

    Integer separatorIndex =
        voiceSelection.indexOf('|')

    if (separatorIndex < 0) {
        String locale =
            voiceLocaleMap()[voiceSelection]

        return [
            name: voiceSelection,
            locale: locale
        ]
    }

    return [
        name: voiceSelection.substring(
            0,
            separatorIndex
        ),
        locale: voiceSelection.substring(
            separatorIndex + 1
        )
    ]
}

Map voiceLocaleMap() {
    return [
        "Ivy": "en-US",
        "Joanna": "en-US",
        "Joey": "en-US",
        "Justin": "en-US",
        "Kendra": "en-US",
        "Kimberly": "en-US",
        "Matthew": "en-US",
        "Salli": "en-US",
        "Nicole": "en-AU",
        "Russell": "en-AU",
        "Amy": "en-GB",
        "Brian": "en-GB",
        "Emma": "en-GB",
        "Raveena": "en-IN",
        "Geraint": "en-GB-WLS",
        "Chantal": "fr-CA",
        "Celine": "fr-FR",
        "Lea": "fr-FR",
        "Mathieu": "fr-FR",
        "Hans": "de-DE",
        "Marlene": "de-DE",
        "Vicki": "de-DE",
        "Bianca": "it-IT",
        "Carla": "it-IT",
        "Giorgio": "it-IT",
        "Mizuki": "ja-JP",
        "Takumi": "ja-JP",
        "Camila": "pt-BR",
        "Ricardo": "pt-BR",
        "Vitoria": "pt-BR",
        "Lupe": "es-US",
        "Miguel": "es-US",
        "Penelope": "es-US",
        "Conchita": "es-ES",
        "Enrique": "es-ES",
        "Lucia": "es-ES",
        "Mia": "es-MX"
    ]
}

Map voiceOptions() {
    return [
        "default": "Default Alexa voice",

        "Ivy": "English (US) — Ivy",
        "Joanna": "English (US) — Joanna",
        "Joey": "English (US) — Joey",
        "Justin": "English (US) — Justin",
        "Kendra": "English (US) — Kendra",
        "Kimberly": "English (US) — Kimberly",
        "Matthew": "English (US) — Matthew",
        "Salli": "English (US) — Salli",

        "Nicole": "English (Australia) — Nicole",
        "Russell": "English (Australia) — Russell",

        "Amy": "English (Britain) — Amy",
        "Brian": "English (Britain) — Brian",
        "Emma": "English (Britain) — Emma",

        "AditiEnglish": "English (India) — Aditi",
        "Raveena": "English (India) — Raveena",
        "Geraint": "English (Wales) — Geraint",

        "Chantal": "French (Canada) — Chantal",
        "Celine": "French (France) — Celine",
        "Lea": "French (France) — Lea",
        "Mathieu": "French (France) — Mathieu",

        "Hans": "German — Hans",
        "Marlene": "German — Marlene",
        "Vicki": "German — Vicki",

        "AditiHindi": "Hindi — Aditi",

        "Bianca": "Italian — Bianca",
        "Carla": "Italian — Carla",
        "Giorgio": "Italian — Giorgio",

        "Mizuki": "Japanese — Mizuki",
        "Takumi": "Japanese — Takumi",

        "Camila": "Portuguese (Brazil) — Camila",
        "Ricardo": "Portuguese (Brazil) — Ricardo",
        "Vitoria": "Portuguese (Brazil) — Vitoria",

        "Lupe": "Spanish (US) — Lupe",
        "Miguel": "Spanish (US) — Miguel",
        "Penelope": "Spanish (US) — Penelope",

        "Conchita": "Spanish (Spain) — Conchita",
        "Enrique": "Spanish (Spain) — Enrique",
        "Lucia": "Spanish (Spain) — Lucia",

        "Mia": "Spanish (Mexico) — Mia"
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

String formatSsmlForLog(String value) {
    return value
        .replace("<", "‹")
        .replace(">", "›")
}
