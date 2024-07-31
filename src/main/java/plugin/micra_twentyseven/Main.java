package plugin.micra_twentyseven;


import command.EnemyDownCommand;
import java.net.http.WebSocket.Listener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public final class Main extends JavaPlugin implements Listener, org.bukkit.event.Listener {

  private Player player;


  @Override
  public void onEnable() {
    EnemyDownCommand enemyDownCommand = new EnemyDownCommand(this);
    Bukkit.getPluginManager().registerEvents(enemyDownCommand, this);
    Bukkit.getPluginManager().registerEvents(this, this);

    getCommand("enemyDown").setExecutor(enemyDownCommand);
  }


  // プレイヤーがマイクラサーバーに参加した時のイベント
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    // 少し遅延させてタイトルを表示
    new BukkitRunnable() {
      @Override
      public void run() {
        player.sendTitle("敵の種類の点数", "", 10, 90, 30);
      }
    }.runTaskLater(this, 20L); // 1秒遅延
  }

}