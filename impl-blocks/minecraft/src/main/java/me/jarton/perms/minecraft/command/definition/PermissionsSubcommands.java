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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;

import java.util.ArrayList;
import java.util.Set;

import static me.jarton.perms.minecraft.command.Elements.messageSender;

final class PermissionsSubcommands {

    private static CommandArgument<Commander, Integer> permissionValueArgument() {
        return CommandArgument.<Commander, Integer>ofType(Integer.class, "state")
            .withParser(Parsers.permissionValue())
            .build();
    }

    private PermissionsSubcommands() {
    }

    static Command.Builder<Commander> permission(final Command.Builder<Commander> builder, final SubjectRefProvider subjectProvider) {
        final Permission cmdPerm = Permission.pex("permission.set");
        final CommandArgument<Commander, String> permissionArg = StringArgument.<Commander>newBuilder("permission")
            .withSuggestionsProvider(Elements.engineCompletions((engine, input) ->
                new ArrayList<>(((PermissionsEx<?>) engine).getRecordingNotifier().getKnownPermissions())))
            .build();
        final CommandArgument<Commander, Integer> permissionValueArg = permissionValueArgument();

        return builder
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.PERMISSION_DESCRIPTION.tr())
            .argument(permissionArg)
            .argument(permissionValueArg)
            .permission(cmdPerm)
            .handler(ctx -> {
                final SubjectRef.ToData<?> subject = subjectProvider.provideData(ctx, cmdPerm);
                final Set<ContextValue<?>> contexts = Elements.contexts(ctx);
                final String permission = ctx.get(permissionArg);
                final int value = ctx.get(permissionValueArg);

                subject.update(contexts, segment -> segment.withPermission(permission, value))
                    .whenComplete(Elements.messageSender(ctx.getSender(), Messages.PERMISSION_SUCCESS.tr(
                        Formats.permission(permission, value),
                        ctx.getSender().formatter().subject(subject),
                        Formats.contexts(contexts)
                    )));
            });
    }

    static Command.Builder<Commander> permissionDefault(final Command.Builder<Commander> builder, final SubjectRefProvider subjectProvider) {
        final Permission cmdPerm = Permission.pex("permission.set-default");
        final CommandArgument<Commander, Integer> permissionValueArg = permissionValueArgument();
        return builder
            .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.PERMISSION_DEFAULT_DESCRIPTION.tr())
            .argument(permissionValueArg)
            .permission(cmdPerm)
            .handler(ctx -> {
                final SubjectRef.ToData<?> subject = subjectProvider.provideData(ctx, cmdPerm);
                final Set<ContextValue<?>> contexts = Elements.contexts(ctx);
                final int value = ctx.get(permissionValueArg);

                subject.update(contexts, segment -> segment.withFallbackPermission(value))
                    .whenComplete(messageSender(ctx.getSender(), Messages.PERMISSION_DEFAULT_SUCCESS.tr(
                        Formats.permissionValue(value),
                        ctx.getSender().formatter().subject(subject).style(ctx.getSender().formatter()::hl),
                        Formats.contexts(contexts)
                    )));
            });
    }

}
