package me.gnat008.perworldinventory.process;

import me.gnat008.perworldinventory.config.SettingsMocker;
import me.gnat008.perworldinventory.data.players.PWIPlayerManager;
import me.gnat008.perworldinventory.groups.Group;
import me.gnat008.perworldinventory.groups.GroupManager;
import me.gnat008.perworldinventory.permission.PermissionManager;
import me.gnat008.perworldinventory.permission.PlayerPermission;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link InventoryChangeProcess}.
 */
@RunWith(MockitoJUnitRunner.class)
public class InventoryChangeProcessTest {

    @InjectMocks
    private InventoryChangeProcess process;

    @Mock
    private GroupManager groupManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PWIPlayerManager playerManager;

    @Test
    public void shouldNotChangeInventoryBecauseFromGroupUnconfigured() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, false);
        Group to = mockGroup("other_group" ,GameMode.SURVIVAL, true);
        SettingsMocker.create()
                .set("share-if-unconfigured", true)
                .save();

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldNotChangeInventoryBecauseToGroupUnconfigured() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group" ,GameMode.SURVIVAL, false);
        SettingsMocker.create()
                .set("share-if-unconfigured", true)
                .save();

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldChangeInventoryEvenIfGroupsNotConfigured() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, false);
        Group to = mockGroup("other_group" ,GameMode.SURVIVAL, false);
        SettingsMocker.create()
                .set("share-if-unconfigured", false)
                .set("disable-bypass", false)
                .set("separate-gamemodeinventories", true)
                .save();
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(permissionManager).hasPermission(player, PlayerPermission.BYPASS_WORLDS);
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotChangeInventoryBecauseSameGroup() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, true);
        Group to = from;

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
        verifyZeroInteractions(permissionManager);
    }

    @Test
    public void shouldNotChangeInventoryBecauseBypass() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group" ,GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group" ,GameMode.SURVIVAL, true);
        SettingsMocker.create()
                .set("disable-bypass", false)
                .save();
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        // when
        process.processWorldChange(player, from, to);

        // then
        verifyZeroInteractions(playerManager);
    }

    @Test
    public void shouldNotBypassBecauseNoPermission() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, true);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", false)
                .save();
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(false);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    @Test
    public void shouldNotBypassBecauseBypassDisabled() {
        // given
        Player player = mock(Player.class);
        Group from = mockGroup("test_group", GameMode.SURVIVAL, true);
        Group to = mockGroup("other_group", GameMode.SURVIVAL, true);
        SettingsMocker.create()
                .set("separate-gamemode-inventories", true)
                .set("disable-bypass", true)
                .save();
        given(permissionManager.hasPermission(player, PlayerPermission.BYPASS_WORLDS)).willReturn(true);

        // when
        process.processWorldChange(player, from, to);

        // then
        verify(playerManager).getPlayerData(any(Group.class), any(GameMode.class), any(Player.class));
    }

    private Group mockGroup(String name, GameMode gameMode, boolean configured) {
        List<String> worlds = new ArrayList<>();
        worlds.add(name);

        return new Group(name, worlds, gameMode, configured);
    }
}
