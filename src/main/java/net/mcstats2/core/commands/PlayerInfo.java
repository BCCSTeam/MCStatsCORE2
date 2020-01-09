package net.mcstats2.core.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.ChatColor;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.api.MCSEntity.MCSPlayer;
import net.mcstats2.core.api.chat.ClickEvent;
import net.mcstats2.core.api.chat.ComponentBuilder;
import net.mcstats2.core.api.chat.HoverEvent;
import net.mcstats2.core.api.chat.TextComponent;
import net.mcstats2.core.api.config.Configuration;
import net.mcstats2.core.network.web.data.MCSPlayerData;
import net.mcstats2.core.utils.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PlayerInfo extends Command {

    public PlayerInfo(String command) {
        super(command);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        if (p.hasPermission("MCStatsNET.pinfo")) {
            Configuration lang = p.getLang();

            if (args.length >= 1) {
                MCSPlayer tp = null;
                try {
                    tp = MCSCore.getInstance().getPlayer(args[0]);
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (tp == null) {
                    p.sendMessage(("§cError with the other Profile!"));
                    return;
                }

                try {
                    String mode = args.length >= 2 ? args[1] : null;

                    if (mode == null) {
                    /*StringBuilder names = new StringBuilder();
                    for (MCSPlayerData.Response.Name name : tp.getNames()) {
                        if (names.length() != 0)
                            names.append(", ");

                        if (name.played)
                            names.append("§6");

                        names.append(name.name);

                        if (name.played)
                            names.append("§e");
                    }*/
                    /*p.sendMessage("§aName: §e" + tp.getName());
                    p.sendMessage("§aNames: §e" + (names.length() == 0 ? "§6" + tp.getName() : names.toString()));
                    p.sendMessage("§8[§cMutes§7] §8[§cBans§7] §8[§aLogins§7] §8[§cChatLogs§7]");*/


                        ComponentBuilder result = new ComponentBuilder("");

                        ComponentBuilder name_details = new ComponentBuilder("UUID: ").color(ChatColor.GREEN)
                                .append(tp.getUUID().toString()).color(ChatColor.YELLOW);
                        ComponentBuilder name = new ComponentBuilder("Name: ").color(ChatColor.GREEN)
                                .append(tp.getName()).color(ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, name_details.create()));
                        result.append(name.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder names = new ComponentBuilder("Names: ").color(ChatColor.GREEN);
                        for (MCSPlayerData.Response.Name data : tp.getNames()) {
                            if (names.size() != 0)
                                names.append(", ").color(ChatColor.YELLOW);

                            ComponentBuilder names_details = new ComponentBuilder("Changed at: ").color(ChatColor.GREEN);
                            if (data.changedToAt == null)
                                names_details.append("NaN").color(ChatColor.GRAY);
                            else
                                names_details.append(data.changedToAt).color(ChatColor.YELLOW);

                            name = new ComponentBuilder(data.name).color(data.played ? ChatColor.GOLD : ChatColor.YELLOW).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, names_details.create()));

                            names.append(name.create(), ComponentBuilder.FormatRetention.NONE);

                        }
                        result.append("\n").append(names.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder buttons = new ComponentBuilder("");

                        ComponentBuilder mutes = new ComponentBuilder("§8[§6Mutes§8]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " mutes"));
                        buttons.append(mutes.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder bans = new ComponentBuilder("§8[§6Bans§8]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " bans"));
                        buttons.append(" ").append(bans.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder logins = new ComponentBuilder("§8[§6Login's§8]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " logins"));
                        buttons.append(" ").append(logins.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder chatLog = new ComponentBuilder("§8[§6ChatLogs§8]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " chatlog"));
                        buttons.append(" ").append(chatLog.create(), ComponentBuilder.FormatRetention.NONE);

                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §8}§m----------");
                        p.sendMessage(result.create());
                        p.sendMessage();
                        p.sendMessage("§aActive punishments:");

                        int count = 0;
                        MCSPlayer.Mute mute = tp.getActiveMute();
                        if (mute != null) {
                            count++;
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", mute.getID());
                            replace.put("reason", mute.getCustomReason() == null ? (mute.getReason() != null ? mute.getReason().getText() : "err") : (mute.getCustomReason().isEmpty() ? "&8&o<none>&r" : mute.getCustomReason()));

                            if (mute.getExpire() != 0) {
                                HashMap<String, Object> expires = new HashMap<>();
                                long endsIn = Math.abs(mute.getExpire());
                                long seconds = (endsIn) % 60;
                                long minutes = (endsIn / 60) % 60;
                                long hours = (endsIn / 60 / 60) % 24;
                                long days = (endsIn / 60 / 60 / 24);
                                expires.put("seconds", seconds);
                                expires.put("minutes", minutes);
                                expires.put("hours", hours);
                                expires.put("days", days);

                                Timestamp end_timestamp = new Timestamp(mute.getTime() + (mute.getExpire() * 1000));
                                expires.put("end_year", end_timestamp.getYear());
                                expires.put("end_month", end_timestamp.getMonth());
                                expires.put("end_date", end_timestamp.getDate());
                                expires.put("end_day", end_timestamp.getDay());
                                expires.put("end_hours", end_timestamp.getHours());
                                expires.put("end_minutes", end_timestamp.getMinutes());
                                expires.put("end_seconds", end_timestamp.getSeconds());

                                replace.put("expires", StringUtils.replace(p.getLang().getString("expires.temporary"), expires));
                            } else
                                replace.put("expires", p.getLang().getString("expires.never"));

                            replace.put("playername", getName());
                            replace.put("staffname", mute.getSTAFF().getName());

                            String status = p.getLang().getString("status.active");
                            if (!mute.isValid())
                                status = p.getLang().getString("status.invalid");
                            else if (mute.isExpired())
                                status = p.getLang().getString("status.expired");
                            replace.put("status", status);

                            ComponentBuilder punish = new ComponentBuilder("");

                            ComponentBuilder text = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', StringUtils.replace(p.getLang().getString("playerInfo.list.mutes.text"), replace)))
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MCSCore.getInstance().buildScreen(p.getLang(), "playerInfo.list.mutes.details", replace))));

                            punish.append(text.create(), ComponentBuilder.FormatRetention.NONE);

                            ComponentBuilder delete = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', p.getLang().getString("actions.unmute")));
                            if (!mute.isExpired() && mute.isValid())
                                punish.append(" ").append(delete.create(), ComponentBuilder.FormatRetention.NONE);

                            p.sendMessage(punish.create());
                        }

                        MCSPlayer.Ban ban = tp.getActiveBan();
                        if (ban != null) {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", ban.getID());
                            replace.put("reason", ban.getCustomReason() == null ? (ban.getReason() != null ? ban.getReason().getText() : "err") : (ban.getCustomReason().isEmpty() ? "&8&o<none>&r" : ban.getCustomReason()));

                            if (ban.getExpire() != 0) {
                                count++;
                                HashMap<String, Object> expires = new HashMap<>();
                                long endsIn = Math.abs(ban.getExpire());
                                long seconds = (endsIn) % 60;
                                long minutes = (endsIn / 60) % 60;
                                long hours = (endsIn / 60 / 60) % 24;
                                long days = (endsIn / 60 / 60 / 24);
                                expires.put("seconds", seconds);
                                expires.put("minutes", minutes);
                                expires.put("hours", hours);
                                expires.put("days", days);

                                Timestamp end_timestamp = new Timestamp(ban.getTime() + (ban.getExpire() * 1000));
                                expires.put("end_year", end_timestamp.getYear());
                                expires.put("end_month", end_timestamp.getMonth());
                                expires.put("end_date", end_timestamp.getDate());
                                expires.put("end_day", end_timestamp.getDay());
                                expires.put("end_hours", end_timestamp.getHours());
                                expires.put("end_minutes", end_timestamp.getMinutes());
                                expires.put("end_seconds", end_timestamp.getSeconds());

                                replace.put("expires", StringUtils.replace(p.getLang().getString("expires.temporary"), expires));
                            } else
                                replace.put("expires", p.getLang().getString("expires.never"));

                            replace.put("playername", getName());
                            replace.put("staffname", ban.getSTAFF().getName());

                            String status = p.getLang().getString("status.active");
                            if (!ban.isValid())
                                status = p.getLang().getString("status.invalid");
                            else if (ban.isExpired())
                                status = p.getLang().getString("status.expired");
                            replace.put("status", status);

                            ComponentBuilder punish = new ComponentBuilder("");

                            ComponentBuilder text = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', StringUtils.replace(p.getLang().getString("playerInfo.list.bans.text"), replace)))
                                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MCSCore.getInstance().buildScreen(p.getLang(), "playerInfo.list.bans.details", replace))));

                            punish.append(text.create(), ComponentBuilder.FormatRetention.NONE);

                            ComponentBuilder delete = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', p.getLang().getString("actions.unmute")));
                            if (!ban.isExpired() && ban.isValid())
                                punish.append(" ").append(delete.create(), ComponentBuilder.FormatRetention.NONE);

                            p.sendMessage(punish.create());
                        }

                        if (count == 0)
                            p.sendMessage("§7<none>");
                        
                        p.sendMessage();
                        p.sendMessage(buttons.create());
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §8}§m----------");
                        p.sendMessage();
                    } else if (mode.equalsIgnoreCase("mutes")) {
                        int actives = tp.countMutes(null, true);
                        int total = tp.countMutes(null, false);
                        int pages = total / 5;

                        int page = 0;
                        if (args.length == 3) {
                            if (!StringUtils.isNumeric(args[2])) {
                                p.sendMessage("§a/pinfo " + tp.getName() + " mutes §e<page>");
                                return;
                            }

                            page = Integer.parseInt(args[2]);
                        }

                        ComponentBuilder buttons = new ComponentBuilder("");

                        ComponentBuilder back = new ComponentBuilder("§7[§a§b⇦§r§7]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " mutes " + (page - 1)));
                        if (page != 0)
                            buttons.append(back.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder cancel = new ComponentBuilder("§7[§c§bX§r§7]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName()));
                        buttons.append(" ").append(cancel.create(), ComponentBuilder.FormatRetention.NONE);

                        ComponentBuilder next = new ComponentBuilder("§7[§a§b⇨§r§7]").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinfo " + tp.getName() + " mutes " + (page + 1)));
                        buttons.append(" ").append(next.create(), ComponentBuilder.FormatRetention.NONE);

                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eMutes §8}§m----------");

                        tp.getMutes(5, 0).forEach(data ->  {
                            HashMap<String, Object> replace = new HashMap<>();
                            replace.put("id", data.getID());
                            replace.put("reason", data.getCustomReason() == null ? (data.getReason() != null ? data.getReason().getText() : "err") : (data.getCustomReason().isEmpty() ? "&8&o<none>&r" : data.getCustomReason()));

                            if (data.getExpire() != 0) {
                                HashMap<String, Object> expires = new HashMap<>();
                                long endsIn = Math.abs(data.getExpire());
                                long seconds = (endsIn) % 60;
                                long minutes = (endsIn / 60) % 60;
                                long hours = (endsIn / 60 / 60) % 24;
                                long days = (endsIn / 60 / 60 / 24);
                                expires.put("seconds", seconds);
                                expires.put("minutes", minutes);
                                expires.put("hours", hours);
                                expires.put("days", days);

                                Timestamp end_timestamp = new Timestamp(data.getTime() + (data.getExpire() * 1000));
                                expires.put("end_year", end_timestamp.getYear());
                                expires.put("end_month", end_timestamp.getMonth());
                                expires.put("end_date", end_timestamp.getDate());
                                expires.put("end_day", end_timestamp.getDay());
                                expires.put("end_hours", end_timestamp.getHours());
                                expires.put("end_minutes", end_timestamp.getMinutes());
                                expires.put("end_seconds", end_timestamp.getSeconds());

                                replace.put("expires", StringUtils.replace(p.getLang().getString("expires.temporary"), expires));
                            } else
                                replace.put("expires", p.getLang().getString("expires.never"));

                            replace.put("playername", getName());
                            replace.put("staffname", data.getSTAFF().getName());

                            String status = p.getLang().getString("status.active");
                            if (!data.isValid())
                                status = p.getLang().getString("status.invalid");
                            else if (data.isExpired())
                                status = p.getLang().getString("status.expired");
                            replace.put("status", status);

                            ComponentBuilder punish = new ComponentBuilder("");

                            ComponentBuilder text = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', StringUtils.replace(p.getLang().getString("playerInfo.list.mutes.text"), replace)))
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(MCSCore.getInstance().buildScreen(p.getLang(), "playerInfo.list.mutes.details", replace))));

                            punish.append(text.create(), ComponentBuilder.FormatRetention.NONE);

                            ComponentBuilder delete = new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', p.getLang().getString("actions.unmute")));
                            if (!data.isExpired() && data.isValid())
                                punish.append(" ").append(delete.create(), ComponentBuilder.FormatRetention.NONE);

                            p.sendMessage(punish.create());
                        });

                        p.sendMessage();
                        p.sendMessage(buttons.create());
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eMutes §8}§m----------");
                        p.sendMessage();

                    } else if (mode.equalsIgnoreCase("bans")) {
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eBans §8}§m----------");
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eBans §8}§m----------");
                        p.sendMessage();

                    } else if (mode.equalsIgnoreCase("logins")) {
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eLogins §8}§m----------");
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eLogins §8}§m----------");
                        p.sendMessage();

                    } else if (mode.equalsIgnoreCase("chatlog")) {
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eChatLogs §8}§m----------");
                        p.sendMessage();
                        p.sendMessage("§8§m----------{§r §a" + tp.getName() + " §7- §eChatLogs §8}§m----------");
                        p.sendMessage();

                    } else
                        p.sendMessage("§a/pinfo " + tp.getName() + " [§emutes/bans/logins/chatlog§a] [page number]");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                p.sendMessage("§a/pinfo <name> [mutes/bans/logins/chatlog] [page number]");
        }
    }


}