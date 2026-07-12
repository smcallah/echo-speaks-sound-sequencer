definition(
    name: "Echo SSML Sequences",
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
        title: "Echo SSML Sequences",
        install: true,
        uninstall: true
    ) {
        section("Sequences") {
            app(
                name: "sequenceChildren",
                appName: "Echo SSML Sequence Child",
                namespace: "monkeyland",
                title: "Add a new Echo sequence",
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
    log.info "Echo SSML Sequences parent installed"
}

void updated() {
    log.info "Echo SSML Sequences parent updated"
}

void uninstalled() {
    log.info "Echo SSML Sequences parent uninstalled"
}