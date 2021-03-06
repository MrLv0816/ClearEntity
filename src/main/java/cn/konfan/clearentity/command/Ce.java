package cn.konfan.clearentity.command;

import cn.konfan.clearentity.ClearEntity;
import cn.konfan.clearentity.task.ClearTask;
import cn.konfan.clearentity.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.*;

public class Ce implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("list", "search", "clear", "reload");
        }
        if (args.length == 2 && "list".equalsIgnoreCase(args[0])) {
            return Arrays.asList("mode", "monster", "animals");
        }
        if (args.length == 2 && "search".equalsIgnoreCase(args[0])) {
            return Arrays.asList("chunk", "entity");
        }
        if (args.length == 3 && "chunk".equalsIgnoreCase(args[1])) {
            return Arrays.asList("10", "100", "1000");
        }
        if (args.length == 3 && "entity".equalsIgnoreCase(args[1])) {
            if (!(sender instanceof Player)) {
                return null;
            }
            List<String> list = new ArrayList<>();
            for (Entity entity : ((Player) sender).getWorld().getEntities()) {
                list.add(Utils.getSaveID(entity));
            }
            return list;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            this.help(sender);
            return false;
        }

        if (!sender.isPermissionSet("ClearEntity.admin") && !"egg".equals(args[0])) {
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                this.reload(sender);
                break;
            case "list":
                this.list(sender, args);
                break;
            case "clear":
                this.clear();
                break;
            case "search":
                this.search(sender, args);
                break;
            case "egg":
                sender.sendMessage("_Godson");
                break;
            default:
                this.help(sender);

        }
        return false;
    }


    /**
     * ??????????????????
     */
    private void clear() {
        ClearTask.clearStart();
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param args ?????????????????????
     */
    private void list(CommandSender sender, String[] args) {
        Map<String, Integer> map = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {

                String saveID = Utils.getSaveID(entity);
                saveID = "".equals(saveID) ? "unknown" : saveID;


                if (args.length == 2) {
                    //????????????????????????
                    if ("mode".equalsIgnoreCase(args[1]) && saveID.startsWith("minecraft:")) {
                        continue;
                    }
                    //???????????????????????????
                    if ("monster".equalsIgnoreCase(args[1]) && !(entity instanceof Monster)) {
                        continue;
                    }
                    //?????????????????????
                    if ("animals".equalsIgnoreCase(args[1]) && !(entity instanceof Animals)) {
                        continue;
                    }
                }

                Integer num = map.get(Utils.getSaveID(entity));
                map.put(saveID, num == null ? 1 : num + 1);
            }
        }

        if (map.keySet().size() == 0) {
            sender.sendMessage(Utils.getMessage("listNoScanner"));
            return;
        }


        sender.sendMessage(Utils.getMessage("listScannerNum").replaceAll("%COUNT%", map.size() + ""));
        try {
            //????????????????????? ?????????????????????
            Class.forName("net.md_5.bungee.api.chat.hover.content.Content");

            for (String s : map.keySet()) {
                net.md_5.bungee.api.chat.BaseComponent url = new TextComponent("- ??b" + s + " ??e" + map.get(s));
                url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Utils.getColorText(Utils.getMessagesConfig().getString("newJsonTextClick")))));
                url.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "  - '" + s + "'"));
                sender.spigot().sendMessage(url);
            }


        } catch (Exception e) {


            for (String s : map.keySet()) {
                BaseComponent url = new TextComponent("- ??b" + s + " ??e" + map.get(s));
                url.setHoverEvent(new HoverEvent
                        (HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Utils.getColorText(Utils.getMessagesConfig().getString("oldJsonTextClick")))}));
                url.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "  - '" + s + "'"));
                sender.spigot().sendMessage(url);
            }


        }
    }

    /**
     * search???????????????
     *
     * @param sender ?????????
     * @param args   ??????
     */
    private void search(CommandSender sender, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("commandNotConsoleRun"));
            return;
        }

        Player player = (Player) sender;
        try {
            switch (args[1].toLowerCase()) {
                case "chunk":
                    this.searchChunk(player, Integer.parseInt(args[2]));
                    break;
                case "entity":
                    this.searchEntity(player, args[2], Integer.parseInt(args[3]));
                    break;
                default:
                    sender.sendMessage(Utils.getMessage("paramError"));
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.getMessage("paramError"));
        }


    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param player ??????
     * @param min    ????????????????????????????????????
     */
    private void searchChunk(Player player, Integer min) {
        int num = 0;
        Chunk[] loadedChunks = player.getWorld().getLoadedChunks();
        for (Chunk loadedChunk : loadedChunks) {
            if (loadedChunk.getEntities().length < min) {
                continue;
            }
            num++;

            HoverEvent hoverEvent = new HoverEvent
                    (HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Utils.getColorText(Utils.getMessagesConfig().getString("clickTpChunk")))});
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + loadedChunk.getX() * 16 + " " + player.getLocation().getY() + " " + loadedChunk.getZ() * 16);
            player.spigot().sendMessage(Utils.jsonText(Utils.getColorText(Utils.getMessagesConfig().getString("showFormat").replaceAll("%CSYS%", "[X:" + loadedChunk.getX() + ",Z:" + loadedChunk.getZ() + "]").replaceAll("%COUNT%", loadedChunk.getEntities().length + "")), hoverEvent, clickEvent));

        }

        if (num == 0) {
            player.sendMessage(Utils.getMessage("noSearchChunk"));
            return;
        }
        player.sendMessage(Utils.getMessage("searchChunk").replaceAll("%COUNT%", num + ""));

    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param player ?????????
     * @param id     ??????id
     * @param min    ????????????
     */
    private void searchEntity(Player player, String id, Integer min) {
        Chunk[] loadedChunks = player.getWorld().getLoadedChunks();
        Map<Chunk, Integer> map = new HashMap<>();
        for (Chunk loadedChunk : loadedChunks) {
            for (Entity entity : loadedChunk.getEntities()) {
                String saveID = Utils.getSaveID(entity);
                if (!saveID.equalsIgnoreCase(id)) {
                    continue;
                }
                Integer entityNum = map.get(loadedChunk);
                map.put(loadedChunk, entityNum == null ? 1 : entityNum + 1);
            }
        }

        if (map.size() == 0) {
            player.sendMessage(Utils.getMessage("noSearchChunk"));
            return;
        }

        int showNum = 0;
        for (Chunk chunk : map.keySet()) {

            if (map.get(chunk) < min) {
                continue;
            }
            showNum++;
            HoverEvent hoverEvent = new HoverEvent
                    (HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(Utils.getColorText(Utils.getMessagesConfig().getString("clickTpChunk")))});
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + chunk.getX() * 16 + " " + player.getLocation().getY() + " " + chunk.getZ() * 16);
            player.spigot().sendMessage(Utils.jsonText(Utils.getColorText(Utils.getMessagesConfig().getString("showFormat").replaceAll("%CSYS%", "[X:" + chunk.getX() + ",Z:" + chunk.getZ() + "]").replaceAll("%COUNT%", map.get(chunk) + "")), hoverEvent, clickEvent));
        }

        if (showNum == 0) {
            player.sendMessage(Utils.getMessage("noSearchChunk"));
            return;
        }
        player.sendMessage(Utils.getMessage("searchChunk").replaceAll("%COUNT%", showNum + ""));

    }


    /**
     * ????????????????????????
     *
     * @param sender ?????????
     */
    private void reload(CommandSender sender) {
        ClearEntity.plugin.reloadConfig();
        Utils.reloadMessage();
        sender.sendMessage(Utils.getMessage("reload"));
    }

    private void help(CommandSender sender) {
//        sender.sendMessage("- ??a[ClearEntity] ??e??????-------------------#");
//        sender.sendMessage("- ??b/ClearEntity clear");
//        sender.sendMessage("- ??e??????????????????????????????");
//        sender.sendMessage("- ??b/ClearEntity list");
//        sender.sendMessage("- ??e????????????????????????????????????");
//        sender.sendMessage("- ??b/ClearEntity search <chunk/entity>");
//        sender.sendMessage("- ??e[chunk]??????????????????????????????????????????");
//        sender.sendMessage("- ??e[entity]????????????????????????????????????????????????");
//        sender.sendMessage("- ??b/ClearEntity reload");
//        sender.sendMessage("- ??e????????????????????????");
//        sender.sendMessage("- ??e#------------------------------------#");
//        sender.sendMessage("- ??a??????????????????: /ce");
        sender.sendMessage(Utils.getColorText(Utils.getMessagesConfig().getString("help")));
    }
}
