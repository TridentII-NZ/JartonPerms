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
package me.jarton.perms.minecraft.command.definition;

import me.jarton.perms.PermissionsEngine;
import me.jarton.perms.minecraft.command.Commander;
import me.jarton.perms.minecraft.command.PEXCommandPreprocessor;
import me.jarton.perms.minecraft.command.Permission;
import me.jarton.perms.subject.CalculatedSubject;
import me.jarton.perms.subject.SubjectRef;
import me.jarton.perms.subject.SubjectType;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import static me.jarton.perms.minecraft.command.Elements.FLAG_TRANSIENT;

@FunctionalInterface
interface SubjectRefProvider {

    static SubjectRefProvider of(final CommandArgument<?, SubjectType<?>> typeArg, final CommandArgument<?, ?> identArg) {
        return new SubjectRefProvider() {
            @Override
            @SuppressWarnings("unchecked")
            public <C> SubjectRef<?> provide(final CommandContext<C> ctx) {
                return SubjectRef.subject(
                    (SubjectType<Object>) ctx.get(typeArg.getName()),
                    ctx.get(identArg.getName())
                );
            }
        };
    }

    <C> SubjectRef<?> provide(final CommandContext<C> ctx);

    /**
     * Retrieve a calculated subject based on the information in the context.
     *
     * @param ctx the command context
     * @param toCheck the base permission, without subject information appended
     * @return a subject
     */
    default CalculatedSubject provideCalculated(final CommandContext<Commander> ctx, final @Nullable Permission toCheck) {
        // Query reference then check permission to act on the data
        final SubjectRef<?> ref = this.provide(ctx);
        if (toCheck != null) {
            ctx.getSender().checkPermission(toCheck.then(ref));
        }

        return ctx.<PermissionsEngine>get(PEXCommandPreprocessor.PEX_ENGINE)
            .subject(ref).join();
    }

    default SubjectRef.ToData<?> provideData(final CommandContext<Commander> ctx, final @Nullable Permission toCheck) {
        final CalculatedSubject calculated = this.provideCalculated(ctx, toCheck);

        if (ctx.flags().isPresent(FLAG_TRANSIENT.getName())) {
            return calculated.transientData();
        } else {
            return calculated.data();
        }
    }

}
