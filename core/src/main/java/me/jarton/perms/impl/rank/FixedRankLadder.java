/*
 * PermissionsEx
 * Copyright (C) zml and PermissionsEx contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jarton.perms.impl.rank;

import me.jarton.perms.impl.util.PCollections;
import me.jarton.perms.rank.RankLadder;
import me.jarton.perms.subject.SubjectRef;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.List;

import static java.util.Map.Entry;

public class FixedRankLadder extends AbstractRankLadder {
    private final PVector<SubjectRef<?>> ranks;

    public FixedRankLadder(String name, List<SubjectRef<?>> ranks) {
        super(name);
        this.ranks = PCollections.asVector(ranks);
    }

    @Override
    public PVector<SubjectRef<?>> ranks() {
        return this.ranks;
    }

    @Override
    protected RankLadder newWithRanks(final PVector<SubjectRef<?>> ents) {
        return new FixedRankLadder(name(), ents);
    }
}
