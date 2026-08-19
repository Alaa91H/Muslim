# -*- coding: utf-8 -*-
"""Convert the mushaf reader's vertical LazyColumn to a HorizontalPager."""
import io

p = "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"
s = io.open(p, encoding="utf-8").read()

old = """                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                viewportTopPx = coords.positionInRoot().y
                                viewportHeightPx = coords.size.height
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                    ) {
                        if (isWide) {
                            // Two mushaf pages per row on wide screens.
                            items(spreads.size, key = { "spread-${spreads[it].first().key}" }) { index ->
                                MushafSpreadRow(
                                    spread = spreads[index],
                                    surahName = state.surah?.arabicName.orEmpty(),
                                    fontSizeSp = fontSize,
                                    playingAyahGlobal = currentAudioAyah,
                                    selectedAyahGlobal = currentAyah?.globalNumber,
                                    tappedAyahGlobal = tappedAyahGlobal,
                                    scrollTargetAyahGlobal = scrollTargetAyah,
                                    onAyahRootTopPx = reportAyahTop,
                                    onClickPage = { pageAyahs ->
                                        viewModel.currentAyah.value = pageAyahs.first()
                                    },
                                    onAyahClick = { ayah ->
                                        // Tap only selects the ayah; recitation
                                        // starts from the play button in the
                                        // recitation bar below.
                                        viewModel.currentAyah.value = ayah
                                        tappedAyahGlobal = ayah.globalNumber
                                    },
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        } else {
                            items(pageEntries.size, key = { pageEntries[it].key }) { index ->
                            val (pageNumber, pageAyahs) = pageEntries[index]
                            MushafPageCard(
                                pageNumber = pageNumber,
                                ayahs = pageAyahs,
                                surahName = state.surah?.arabicName.orEmpty(),
                                fontSizeSp = fontSize,
                                playingAyahGlobal = currentAudioAyah,
                                selectedAyahGlobal = currentAyah?.globalNumber,
                                tappedAyahGlobal = tappedAyahGlobal,
                                scrollTargetAyahGlobal = scrollTargetAyah,
                                onAyahRootTopPx = reportAyahTop,
                                onClick = { viewModel.currentAyah.value = pageAyahs.first() },
                                onAyahClick = { ayah ->
                                    // Tap only selects the ayah; recitation
                                    // starts from the play button in the
                                    // recitation bar below.
                                    viewModel.currentAyah.value = ayah
                                    tappedAyahGlobal = ayah.globalNumber
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                            }
                        }
                    }"""

new = """                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                viewportTopPx = coords.positionInRoot().y
                                viewportHeightPx = coords.size.height
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                        pageSpacing = 12.dp,
                    ) { pageIndex ->
                        // Each pager page keeps its own vertical scroll for
                        // content taller than the screen; the follow-along
                        // adjustment scrolls this state.
                        val pageScroll = remember { ScrollState(0) }
                        pageScrollStates[pageIndex] = pageScroll
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(pageScroll),
                        ) {
                            if (isWide) {
                                // Two mushaf pages per pager page on wide screens.
                                val spread = spreads.getOrNull(pageIndex)
                                if (spread != null) {
                                    MushafSpreadRow(
                                        spread = spread,
                                        surahName = state.surah?.arabicName.orEmpty(),
                                        fontSizeSp = fontSize,
                                        playingAyahGlobal = currentAudioAyah,
                                        selectedAyahGlobal = currentAyah?.globalNumber,
                                        tappedAyahGlobal = tappedAyahGlobal,
                                        scrollTargetAyahGlobal = scrollTargetAyah,
                                        onAyahRootTopPx = reportAyahTop,
                                        onClickPage = { pageAyahs ->
                                            viewModel.currentAyah.value = pageAyahs.first()
                                        },
                                        onAyahClick = { ayah ->
                                            // Tap only selects the ayah; recitation
                                            // starts from the play button in the
                                            // recitation bar below.
                                            viewModel.currentAyah.value = ayah
                                            tappedAyahGlobal = ayah.globalNumber
                                        },
                                    )
                                }
                            } else {
                                val (pageNumber, pageAyahs) = pageEntries[pageIndex]
                                MushafPageCard(
                                    pageNumber = pageNumber,
                                    ayahs = pageAyahs,
                                    surahName = state.surah?.arabicName.orEmpty(),
                                    fontSizeSp = fontSize,
                                    playingAyahGlobal = currentAudioAyah,
                                    selectedAyahGlobal = currentAyah?.globalNumber,
                                    tappedAyahGlobal = tappedAyahGlobal,
                                    scrollTargetAyahGlobal = scrollTargetAyah,
                                    onAyahRootTopPx = reportAyahTop,
                                    onClick = { viewModel.currentAyah.value = pageAyahs.first() },
                                    onAyahClick = { ayah ->
                                        // Tap only selects the ayah; recitation
                                        // starts from the play button in the
                                        // recitation bar below.
                                        viewModel.currentAyah.value = ayah
                                        tappedAyahGlobal = ayah.globalNumber
                                    },
                                )
                            }
                        }
                    }"""

n = s.count(old)
print("count:", n)
if n == 1:
    s = s.replace(old, new)
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("REPLACED")
else:
    print("NOT REPLACED")
