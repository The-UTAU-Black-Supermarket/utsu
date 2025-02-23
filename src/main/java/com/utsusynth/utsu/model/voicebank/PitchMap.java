package com.utsusynth.utsu.model.voicebank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.utsusynth.utsu.common.utils.PitchUtils;

public class PitchMap {
    private final ImmutableList<String> pitches;
    private final Map<String, String> prefixes;
    private final Map<String, String> suffixes;

    public PitchMap() {
        prefixes = new HashMap<>();
        suffixes = new HashMap<>();
        ImmutableList.Builder<String> pitchBuilder = ImmutableList.builder();
        for (int octave = 7; octave > 0; octave--) {
            for (String pitch : PitchUtils.REVERSE_PITCHES) {
                pitchBuilder.add(pitch + octave);
            }
        }
        pitches = pitchBuilder.build();
    }

    public String getPrefix(String pitch) {
        if (prefixes.containsKey(pitch)) {
            return prefixes.get(pitch);
        }
        return "";
    }

    public String getSuffix(String pitch) {
        if (suffixes.containsKey(pitch)) {
            return suffixes.get(pitch);
        }
        return "";
    }

    public void putPrefix(String pitch, String prefix) {
        prefixes.put(pitch, prefix);
    }

    public void putSuffix(String pitch, String suffix) {
        suffixes.put(pitch, suffix);
    }

    public Iterator<String> getOrderedPitches() {
        return pitches.iterator();
    }
}
