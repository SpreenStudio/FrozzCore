package me.thejokerdev.frozzcore.api.board;

import me.thejokerdev.frozzcore.SpigotMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class BoardAPI {

    public void scoredSidebar(Player player, String title, LinkedHashMap<String, Integer> lines) {
        if (title == null) {
            title = "Unamed board";
        }

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        String lineStr;
        label39:
        for(; lines.size() > 16; lines.remove(lineStr)) {
            lineStr = (String)lines.keySet().toArray()[0];
            int slot = lines.get(lineStr);
            Iterator<String> linesIterator = lines.keySet().iterator();

            while(true) {
                String str;
                do {
                    if (!linesIterator.hasNext()) {
                        continue label39;
                    }

                    str = linesIterator.next();
                } while(lines.get(str) >= slot && (lines.get(str) != slot || str.compareTo(lineStr) >= 0));

                lineStr = str;
                slot = lines.get(str);
            }
        }

        String finalTitle = title;
        Bukkit.getScheduler().runTask(SpigotMain.getPlugin(), () -> {
            if (player != null && player.isOnline()) {
                if (Bukkit.getScoreboardManager().getMainScoreboard() != null && Bukkit.getScoreboardManager().getMainScoreboard() == player.getScoreboard()) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }

                if (player.getScoreboard() == null) {
                    player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                }

                Bukkit.getScheduler().runTaskAsynchronously(SpigotMain.getPlugin(), () -> {
                    Objective scoreboard = player.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
                    if (scoreboard == null) {
                        scoreboard = player.getScoreboard().registerNewObjective(finalTitle.length() > 16 ? finalTitle.substring(0, 15) : finalTitle, "dummy");
                    }

                    scoreboard.setDisplayName(finalTitle);
                    if (scoreboard.getDisplaySlot() == null || scoreboard.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                        scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
                    }

                    Iterator<String> linesIterator = lines.keySet().iterator();

                    while(true) {
                        String str;
                        do {
                            if (!linesIterator.hasNext()) {
                                linesIterator = player.getScoreboard().getEntries().iterator();

                                while(linesIterator.hasNext()) {
                                    str = linesIterator.next();
                                    if (scoreboard.getScore(str).isScoreSet() && !lines.containsKey(str)) {
                                        player.getScoreboard().resetScores(str);
                                    }
                                }

                                return;
                            }

                            str = linesIterator.next();
                        } while(scoreboard.getScore(str).isScoreSet() && scoreboard.getScore(str).getScore() == lines.get(str));

                        scoreboard.getScore(str).setScore(lines.get(str));
                    }
                });
            }
        });
    }

    public LinkedHashMap<String, Integer> getLinkedHashMap(LinkedList<String> list){
        int slot = list.size();
        LinkedHashMap<String, Integer> hashMap = new LinkedHashMap<>();
        for(Iterator<String> var4 = list.iterator(); var4.hasNext(); --slot) {
            String line = var4.next();
            hashMap.put(fixDuplicates(hashMap, line), slot);
        }
        return hashMap;
    }

    private String fixDuplicates(LinkedHashMap<String, Integer> linesHashmap, String line) {
        while(linesHashmap.containsKey(line)) {
            line = line + "§r";
        }

        if (line.length() > 40) {
            line = line.substring(0, 39);
        }

        return line;
    }
}
