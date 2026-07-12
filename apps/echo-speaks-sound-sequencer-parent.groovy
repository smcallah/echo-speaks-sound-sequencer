definition(
    name: "Echo Speaks Sound Sequencer",
    namespace: "monkeyland",
    author: "Steven Callahan",
    description: "Creates and manages ordered Echo Speaks SSML sequences.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    singleInstance: true
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
                namespace: "monkeyland",
                title: "Add a new Echo Speaks sound sequence",
                multiple: true
            )
        }

        section("About") {
            paragraph(
                "Each sequence can contain any ordered combination of " +
                "messages and sounds. Create one child sequence for each " +
                "virtual-switch automation."
            )
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