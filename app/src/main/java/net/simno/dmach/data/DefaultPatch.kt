package net.simno.dmach.data

fun defaultPatch(): Patch {
    val bd = Channel(
        name = "bd",
        settings = listOf(
            Setting(hText = "Pitch A", vText = "Gain", hIndex = 0, vIndex = 7, x = .4f, y = .49f),
            Setting(hText = "Low-pass", vText = "Square", hIndex = 5, vIndex = 3, x = .7f, y = 0f),
            Setting(hText = "Pitch B", vText = "Curve Time", hIndex = 1, vIndex = 2, x = .4f, y = .4f),
            Setting(hText = "Decay", vText = "Noise Level", hIndex = 6, vIndex = 4, x = .49f, y = .7f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    val sd = Channel(
        name = "sd",
        settings = listOf(
            Setting(hText = "Pitch", vText = "Gain", hIndex = 0, vIndex = 9, x = .49f, y = .45f),
            Setting(hText = "Low-pass", vText = "Noise", hIndex = 7, vIndex = 1, x = .6f, y = .8f),
            Setting(hText = "X-fade", vText = "Attack", hIndex = 8, vIndex = 6, x = .35f, y = .55f),
            Setting(hText = "Decay", vText = "Body Decay", hIndex = 4, vIndex = 5, x = .55f, y = .42f),
            Setting(hText = "Band-pass", vText = "Band-pass Q", hIndex = 2, vIndex = 3, x = .7f, y = .6f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    val cp = Channel(
        name = "cp",
        settings = listOf(
            Setting(hText = "Pitch", vText = "Gain", hIndex = 0, vIndex = 7, x = .55f, y = .3f),
            Setting(hText = "Delay 1", vText = "Delay 2", hIndex = 4, vIndex = 5, x = .3f, y = .3f),
            Setting(hText = "Decay", vText = "Filter Q", hIndex = 6, vIndex = 1, x = .59f, y = .2f),
            Setting(hText = "Filter 1", vText = "Filter 2", hIndex = 2, vIndex = 3, x = .9f, y = .15f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    val tt = Channel(
        name = "tt",
        settings = listOf(
            Setting(hText = "Pitch", vText = "Gain", hIndex = 0, vIndex = 1, x = .49f, y = .49f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    val cb = Channel(
        name = "cb",
        settings = listOf(
            Setting(hText = "Pitch", vText = "Gain", hIndex = 0, vIndex = 5, x = .3f, y = .49f),
            Setting(hText = "Decay 1", vText = "Decay 2", hIndex = 1, vIndex = 2, x = .1f, y = .75f),
            Setting(hText = "Vcf", vText = "Vcf Q", hIndex = 3, vIndex = 4, x = .3f, y = 0f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    val hh = Channel(
        name = "hh",
        settings = listOf(
            Setting(hText = "Pitch", vText = "Gain", hIndex = 0, vIndex = 11, x = .45f, y = .4f),
            Setting(hText = "Low-pass", vText = "Snap", hIndex = 10, vIndex = 5, x = .8f, y = .1f),
            Setting(hText = "Noise Pitch", vText = "Noise", hIndex = 4, vIndex = 3, x = .55f, y = .6f),
            Setting(hText = "Ratio B", vText = "Ratio A", hIndex = 2, vIndex = 1, x = .9f, y = 1f),
            Setting(hText = "Release", vText = "Attack", hIndex = 7, vIndex = 6, x = .55f, y = .4f),
            Setting(hText = "Filter", vText = "Filter Q", hIndex = 8, vIndex = 9, x = .7f, y = .6f)
        ),
        selectedSetting = 0,
        pan = Pan(.5f)
    )
    return Patch(
        title = "untitled",
        sequence = Patch.EMPTY_SEQUENCE,
        channels = listOf(bd, sd, cp, tt, cb, hh),
        selectedChannel = Channel.NONE_ID,
        tempo = Tempo(120),
        swing = Swing(0),
        steps = Steps(16)
    )
}
