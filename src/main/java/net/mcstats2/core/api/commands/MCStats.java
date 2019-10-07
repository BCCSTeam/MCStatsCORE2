package net.mcstats2.core.api.commands;

import net.mcstats2.core.MCSCore;
import net.mcstats2.core.api.Command;
import net.mcstats2.core.api.MCSEntity.MCSEntity;
import net.mcstats2.core.network.web.data.MCSUpdaterData;
import net.mcstats2.core.utils.version.Version;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MCStats extends Command {

    public MCStats(String name) {
        super(name);
    }

    @Override
    public void execute(MCSEntity p, String[] args) {
        try {
            if (p.hasPermission("MCStatsNET.admin")) {
                if (args.length >= 1) {
                    if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                        p.sendMessage("§a/mcstats help");
                        p.sendMessage("§a/mcstats version");
                        p.sendMessage("§a/mcstats update");
                        p.sendMessage("§a/mcstats reload");
                        p.sendMessage("§a/mcstats createtemplate <mute/ban> <name> <template-power [1-255]> <times> <reason>");
                        p.sendMessage("§a/mcstats updatetemplate <mute/ban> <id/name> <name/reason/time/power> <value>");
                        p.sendMessage("§a/mcstats deletetemplate <mute/ban> &a<id/name>");

                    } else if (args.length == 1 && args[0].equalsIgnoreCase("version")) {
                        MCSUpdaterData data = MCSCore.getInstance().UPDATE;

                        if (data == null)
                            p.sendMessage("ERROR with loading data...");

                        if (data.getSystem().getStatus() != 200)
                            p.sendMessage(data.getSystem().getMessage());

                        if (!MCSCore.getInstance().API_PARSER_VERSION.isEqual(data.getResponse().getVersion())) {
                            if (MCSCore.getInstance().API_PARSER_VERSION.isHigherThan(data.getResponse().getVersion()))
                                p.sendMessage("§cVersion error! Please check manual for Updates, in cause if this error stay please contact support at support@mcstats.net and wait for future instructions!");

                            p.sendMessage("§6An new version(" + data.getResponse().getVersion() + ") is available for download! Downloading...");
                            p.sendMessage("§aLink: " + data.getResponse().getDownloadURL());
                        } else
                            p.sendMessage("§aRunning newest version(" + data.getResponse().getVersion() + ")!");

                    } else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {

                    } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                        p.sendMessage("§6Reloading...");
                        MCSCore.getInstance().reload();
                        p.sendMessage("§aReload completed!");

                    } else if (args[0].equalsIgnoreCase("createtemplate")) {
                        if (args.length < 6) {
                            p.sendMessage("§a/mcstats createtemplate <mute/ban> <name> <power [1-255]> <times> <reason>");
                            return;
                        }

                        if (!args[1].equalsIgnoreCase("mute") && !args[1].equalsIgnoreCase("ban")) {
                            p.sendMessage("§a/mcstats createtemplate §6<mute/ban> §a<name> <power [1-255]> <times> <reason>");
                            return;
                        }

                        if (!StringUtils.isNumeric(args[3])) {
                            p.sendMessage("§a/mcstats createtemplate " + args[1] + " " + args[2] + " §6<power [1-255]> §a<times> <reason>");
                            return;
                        }

                        if (!(Integer.parseInt(args[3]) >= 1 && Integer.parseInt(args[3]) <= 255)) {
                            p.sendMessage("§a/mcstats createtemplate " + args[1] + " " + args[2] + " <power [§61-255§a]> <times> <reason>");
                            return;
                        }

                        List<Integer> expires = new ArrayList<>();
                        for (String a : args[4].split(",")) {
                            int b = MCSCore.getExpire(a);

                            if (b == -1) {
                                p.sendMessage("§a/mcstats createtemplate " + args[1] + " " + args[2] + " " + args[3] + " §6<times> §a<reason>");
                                return;
                            }

                            expires.add(b);
                        }

                        StringBuilder reason = new StringBuilder();
                        for (int i = 5; i < args.length; i++) {
                            if (reason.length() != 0)
                                reason.append(" ");
                            reason.append(args[i]);
                        }

                        if (args[1].equalsIgnoreCase("mute")) {
                            if (MCSCore.getInstance().createMuteTemplate(args[2], reason.toString(), Integer.parseInt(args[3]), expires) != null)
                                p.sendMessage("§aTemplate created!");
                            else
                                p.sendMessage("§cCreating failed!");

                        } else if (args[1].equalsIgnoreCase("ban")) {
                            if (MCSCore.getInstance().createBanTemplate(args[2], reason.toString(), Integer.parseInt(args[3]), expires) != null)
                                p.sendMessage("§aTemplate created!");
                            else
                                p.sendMessage("§cCreating failed!");

                        }
                    } else if (args[0].equalsIgnoreCase("updatetemplate")) {
                        if (args.length < 4) {
                            p.sendMessage("§a/mcstats updatetemplate <mute/ban> <id/name> <name/reason/time/power> <value>");
                            return;
                        }

                        String a = "UPDATE ";
                        int b = 0;

                        switch (args[1].toLowerCase()) {
                            case "mute":
                                if (MCSCore.getInstance().getMuteTemplateByID(args[2]) == null) {
                                    b = 1;
                                    if (MCSCore.getInstance().getMuteTemplateByName(args[2]) == null) {
                                        p.sendMessage("§&cThis template does not exist!");
                                        return;
                                    }
                                }

                                a += "`MCSCore__mutes-templates` ";
                                break;
                            case "ban":
                                if (MCSCore.getInstance().getBanTemplateByID(args[2]) == null) {
                                    b = 1;
                                    if (MCSCore.getInstance().getBanTemplateByName(args[2]) == null) {
                                        p.sendMessage("§cThis template does not exist!");
                                        return;
                                    }
                                }

                                a += "`MCSCore__bans-templates` ";
                                break;
                            default:
                                p.sendMessage("§a/mcstats updatetemplate §6<mute/ban> §a<id/name> <name/reason/time/power> <value>");
                                return;
                        }

                        switch (args[3].toLowerCase()) {
                            case "name":
                                a += "name=? ";
                                break;
                            case "reason":
                                a += "text=? ";
                                break;
                            case "time":
                                a += "time=? ";
                                break;
                            case "power":
                                if (!StringUtils.isNumeric(args[4])) {
                                    p.sendMessage("§a/mcstats updatetemplate " + args[1].toLowerCase() + " " + args[2] + " power §a<number>");
                                    return;
                                }

                                a += "power=? ";
                                break;
                            default:
                                p.sendMessage("§a/mcstats updatetemplate " + args[1].toLowerCase() + " " + args[2] + " §6<name/reason/time/power> §a<value>");
                                return;
                        }

                        if (b == 0)
                            a += "WHERE id=?";
                        else if (b == 1)
                            a += "WHERE name=?";

                        if (MCSCore.getInstance().getMySQL().queryUpdate(a, args[4], args[2]) != 0)
                            p.sendMessage("§aTemplate deleted!");
                        else
                            p.sendMessage("§cFailed to delete Template!");

                    } else if (args[0].equalsIgnoreCase("deletetemplate")) {
                        if (args.length != 3) {
                            p.sendMessage("§a/mcstats deletetemplate <mute/ban> §a<id/name>");
                            return;
                        }

                        String a = "DELETE FROM ";
                        int b = 0;
                        switch (args[1].toLowerCase()) {
                            case "mute":
                                if (MCSCore.getInstance().getMuteTemplateByID(args[2]) == null) {
                                    b = 1;
                                    if (MCSCore.getInstance().getMuteTemplateByName(args[2]) == null) {
                                        p.sendMessage("§cThis template does not exist!");
                                        return;
                                    }
                                }

                                a += "`MCSCore__mutes-templates` ";
                                break;
                            case "ban":
                                if (MCSCore.getInstance().getBanTemplateByID(args[2]) == null) {
                                    b = 1;
                                    if (MCSCore.getInstance().getBanTemplateByName(args[2]) == null) {
                                        p.sendMessage("§cThis template does not exist!");
                                        return;
                                    }
                                }

                                a += "`MCSCore__bans-templates` ";
                                break;
                            default:
                                p.sendMessage("§a/mcstats deletetemplate §6<mute/ban> §a<id/name>");
                                return;
                        }

                        if (b == 0)
                            a += "WHERE id=?";
                        else if (b == 1)
                            a += "WHERE name=?";

                        if (MCSCore.getInstance().getMySQL().queryUpdate(a, args[2]) != 0)
                            p.sendMessage("§aTemplate deleted!");
                        else
                            p.sendMessage("§cFailed to delete Template!");

                    } else {
                        Version ver = MCSCore.getInstance().API_PARSER_VERSION;
                        p.sendMessage("§a/mcstats help");
                        p.sendMessage();
                        p.sendMessage("§aRunning MCSCore version " + ver.getOriginalString());
                        p.sendMessage();
                        p.sendMessage("§aWebsite: https://www.mcstats.net/");
                        p.sendMessage("§aWiki: https://wiki.mcstats.net/MCStatsCORE2");
                    }
                } else {
                    Version ver = MCSCore.getInstance().API_PARSER_VERSION;
                    p.sendMessage("§a/mcstats help");
                    p.sendMessage();
                    p.sendMessage("§aRunning MCSCore version " + ver.getOriginalString());
                    p.sendMessage();
                    p.sendMessage("§aWebsite: https://www.mcstats.net/");
                    p.sendMessage("§aWiki: https://wiki.mcstats.net/MCStatsCORE2");
                }
            } else {
                Version ver = MCSCore.getInstance().API_PARSER_VERSION;
                p.sendMessage("§aRunning MCSCore version " + ver.getOriginalString());
                p.sendMessage();
                p.sendMessage("§aWebsite: https://www.mcstats.net/");
                p.sendMessage("§aWiki: https://wiki.mcstats.net/MCStatsCORE2");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
