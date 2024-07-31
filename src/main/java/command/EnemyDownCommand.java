package command;

import data.PlayerScore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.micra_twentyseven.Main;


public class EnemyDownCommand implements CommandExecutor, Listener {

  private final Main main;

  private final List<PlayerScore> playerScoreList = new ArrayList<>();

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] strings) {
    if (commandSender instanceof Player player) {
      PlayerScore nowPlayer = getPlayerScore(player);

      nowPlayer.setGameTime(60);
      World world = player.getWorld();

      initPlayerStatus(player);

      // 時間制限
      Bukkit.getScheduler().runTaskTimer(main, task -> {
        // 時間制限キャンセル
        if (nowPlayer.getGameTime() <= 0) {
          task.cancel();
          player.sendTitle("ゲーム終了",
              nowPlayer.getPlayerName() + "合計" + nowPlayer.getScore() + "点",
              0, 30, 0);
//          タイマー再設定
          nowPlayer.setScore(0);
          return;
        }
        // 時間制限開始
        world.spawnEntity(getEnemySpawnLocation(player, world), getEnemy());
        nowPlayer.setGameTime(nowPlayer.getGameTime() - 5);
      }, 0, 5 * 20);
    }
    return true;
  }


  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e) {
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();
    // イベント実行前と実行後で、プレイヤーがNullであれば、スキップする(Null対策)
    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    /**
     * point　→倒した敵モンスターの点数
     */
    for (PlayerScore playerScore : playerScoreList) {
      if (playerScore.getPlayerName().equals(player.getName())) {
//        デフォルト点数
         int point = switch (enemy.getType()) {
           case ZOMBIE, ZOMBIFIED_PIGLIN  -> 10;
           case SPIDER -> 15;
           case SKELETON -> 30;
           default -> 0;
         };
        playerScore.setScore(playerScore.getScore() + point);
        player.sendMessage(ChatColor.YELLOW +"敵を倒した！ 現在のスコアは、" + playerScore.getScore() + "点です。");
      }
    }
  }

  /**
   * 現在実行しているプレイヤースコア情報を取得する
   *
   * @param player 　コマンドを実行したプレイヤー
   * @return　現在実行しているプレイヤーのスコア情報
   */
  private PlayerScore getPlayerScore(Player player) {
    if (playerScoreList.isEmpty()) {
      return addNewPlayer(player);
    } else {
      for (PlayerScore playerScore : playerScoreList) {
        if (playerScore.getPlayerName().equals(player.getName())) {
          return playerScore; // ここで該当する PlayerScore を返す
        }
      }
      return addNewPlayer(player); // 該当するプレイヤーがいなければ新規作成
    }
  }


  /**
   * 新規プレイヤー追加
   *
   * @param player 　コマンド実行プレイヤー
   * @return　新規プレイヤー
   */
  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  private void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);
    player.setAllowFlight(true);
    player.setFlying(true);
    player.setFlySpeed(1f);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
  }

  private Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;

    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;

    return new Location(world, x, y, z);
  }

  private EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE, EntityType.SPIDER,
        EntityType.SKELETON, EntityType.ZOMBIFIED_PIGLIN);
    int random = new SplittableRandom().nextInt(enemyList.size());
    return enemyList.get(random);
  }
}