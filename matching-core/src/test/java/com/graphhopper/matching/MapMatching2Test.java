/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.matching;

import com.graphhopper.PathWrapper;
import static com.graphhopper.matching.MapMatchingTest.SINGLETON;
import com.graphhopper.matching.MapMatchingTest.TestGraphHopper;
import static com.graphhopper.matching.MapMatchingTest.fetchStreets;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.*;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PathMerger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Peter Karich
 */
public class MapMatching2Test {    

    @Test
    public void testIssue13() {        
        CarFlagEncoder encoder = new CarFlagEncoder();
        TestGraphHopper hopper = new TestGraphHopper();
        hopper.setDataReaderFile("../map-data/map-issue13.osm.gz");
        hopper.setGraphHopperLocation("../target/mapmatchingtest-13");
        hopper.setEncodingManager(new EncodingManager(encoder));
        hopper.importOrLoad();       

        GraphHopperStorage graph = hopper.getGraphHopperStorage();
        LocationIndexMatch locationIndex = new LocationIndexMatch(graph,
                (LocationIndexTree) hopper.getLocationIndex());

        MapMatching mapMatching = new MapMatching(graph, locationIndex, encoder);
       
        List<GPXEntry> inputGPXEntries = new GPXFile().
                doImport("./src/test/resources/issue-13.gpx").getEntries();
        MatchResult mr = mapMatching.doWork(inputGPXEntries);

        // make sure no virtual edges are returned
        int edgeCount = hopper.getGraphHopperStorage().getAllEdges().getMaxId();
        for (EdgeMatch em : mr.getEdgeMatches()) {
            assertTrue("result contains virtual edges:" + em.getEdgeState().toString(),
                    em.getEdgeState().getEdge() < edgeCount);
        }

        // create street names
        assertEquals(Arrays.asList("", "", ""),
                fetchStreets(mr.getEdgeMatches()));
        assertEquals(mr.getGpxEntriesLength(), mr.getMatchLength(), 1.5);
        assertEquals(mr.getGpxEntriesMillis(), mr.getMatchMillis());

        Path path = mapMatching.calcPath(mr);
        PathWrapper matchGHRsp = new PathWrapper();
        new PathMerger().doWork(matchGHRsp, Collections.singletonList(path), SINGLETON.get("en"));
        InstructionList il = matchGHRsp.getInstructions();
        assertEquals(il.toString(), 1, il.size());
    }
}
