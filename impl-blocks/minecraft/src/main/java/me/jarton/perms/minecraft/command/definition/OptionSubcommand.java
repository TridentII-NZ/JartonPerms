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

import me.jarton.perms.context.ContextValue;
import me.jarton.perms.impl.PermissionsEx;
import me.jarton.perms.minecraft.command.Commander;
import me.jarton.perms.minecraft.command.Elements;
import me.jarton.perms.minecraft.command.Formats;
import me.jarton.perms.minecraft.command.Permission;
import me.jarton.perms.minecraft.command.argument.Parsers;
import me.jarton.perms.subject.SubjectRef;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.minecraft.extras.RichDescription;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Set;

import static me.jarton.perms.minecraft.command.Elements.*;

final class OptionSubcommand {
    private OptionSubcommand() {
    }

    static Command.Builder<Commander> register(final Command.Builder<Commander> builder, final SubjectRefProvider subjectProvider) {
        final Permission permission = Permission.pex("option.set");
        final CommandArgument<Commander, String> optionArg = StringArgument.<Commander>newBuilder("option").single()
            .withSuggestionsProvider(engineCompletions((engine, input) -> new ArrayList<>(((PermissionsEx<?>) engine).getRecordingNotifier().getKnownOptions())))
            .build();
        final CommandArgument<Commander, String> optionValueArg = CommandArgument.<Commander, String>ofType(String.class, "value")
            .withParser(Parsers.optionValue())
            .asOptional()
            .build();

       return builder
           .permission(permission)
           .argument(optionArg, RichDescription.of(Messages.OPTION_ARG_OPTION_DESCRIPTION))
           .argument(optionValueArg)
           .handler(ctx -> {
               final SubjectRef.ToData<?> subject = subjectProvider.provideData(ctx, permission);
               final Set<ContextValue<?>> contexts = Elements.contexts(ctx);
               final String option = ctx.get(optionArg);
               final @Nullable String value = ctx.getOrDefault(optionValueArg, null);
               if (value == null) {
                   // Unset an option
                   subject.update(contexts, old -> old.withoutOption(option))
                       .whenComplete(Elements.messageSender(ctx.getSender(), Messages.OPTION_SUCCESS_UNSET.tr(
                           option,
                           ctx.getSender().formatter().subject(subject),
                           Formats.contexts(contexts)
                       )));
               } else {
                   // Set an option
                   subject.update(contexts, old -> old.withOption(option, value))
                       .whenComplete(messageSender(ctx.getSender(), Messages.OPTION_SUCCESS_SET.tr(
                           option,
                           ctx.getSender().formatter().subject(subject),
                           Formats.contexts(contexts)
                       )));
               }
           });
    }

}
