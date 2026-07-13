/**
 * Echo Speaks Sound Sequencer parent app
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

@Field static final String APP_VERSION = "26.3.6"

definition(
    name: "Echo Speaks Sound Sequencer",
    namespace: "smcallah",
    author: "Steven Callahan",
    description: "Creates and manages ordered Echo Speaks SSML sequences.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: true,
    importUrl:
        "https://raw.githubusercontent.com/smcallah/" +
        "echo-speaks-sound-sequencer/main/apps/" +
        "echo-speaks-sound-sequencer-parent.groovy",
    documentationLink:
        "https://github.com/smcallah/echo-speaks-sound-sequencer"
)

preferences {
    page(name: "mainPage")
}

Map mainPage() {
    dynamicPage(
        name: "mainPage",
        title: "Echo Speaks Sound Sequencer",
        install: true,
        uninstall: true
    ) {
        section("Sequences") {
            app(
                name: "sequenceChildren",
                appName: "Echo Speaks Sound - Sequence",
                namespace: "smcallah",
                title: "Add a new Echo Speaks sound sequence",
                multiple: true
            )
        }

        section("About") {
            paragraph(
                "Each sequence can contain any ordered combination of " +
                "messages and sounds. Create one child sequence for each " +
                "switch-triggered automation."
            )
            paragraph("Version ${APP_VERSION}")
        }
    }
}

void installed() {
    log.info "Echo Speaks Sound Sequencer parent installed"
}

void updated() {
    log.info "Echo Speaks Sound Sequencer parent updated"
}

void uninstalled() {
    log.info "Echo Speaks Sound Sequencer parent uninstalled"
}
