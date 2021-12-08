package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day22 implements IPuzzle {
    private static final Pattern PATTERN = Pattern.compile("\\s*Hit Points:\\s*(?<hitPoints>[0-9]+)\\s*Damage:\\s*(?<damage>[0-9]+)\\s*");

    private static final int MAGIC_MISSILE_MANA_COST = 53;
    private static final int DRAIN_MANA_COST = 73;
    private static final int SHIELD_MANA_COST = 113;
    private static final int POISON_MANA_COST = 173;
    private static final int RECHARGE_MANA_COST = 229;

    private static final int SHIELD_TURNS = 6;
    private static final int POISON_TURNS = 6;
    private static final int RECHARGE_TURNS = 5;

    private static final int MAGIC_MISSILE_DAMAGE = 4;
    private static final int DRAIN_TRANSFER = 2;
    private static final int SHIELD_ARMOR_INCREASE = 7;
    private static final int POISON_DAMAGE = 3;
    private static final int RECHARGE_MANA_INCREASE = 101;

    private static final int PLAYER_INITIAL_HITPOINTS = 50;
    private static final int PLAYER_INITIAL_MANA = 500;

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Matcher matcher = PATTERN.matcher(new String(inputCharacters));
        if (!matcher.matches()) {
            throw new IllegalStateException("Cannot parse input");
        }
        int bossHitpoints = Integer.parseInt(matcher.group("hitPoints"));
        int bossDamage = Integer.parseInt(matcher.group("damage"));
        return new BasicPuzzleResults<>(
            simulatePlayerTurn(false,bossHitpoints,bossDamage,50,500,0,0,0,0, Integer.MAX_VALUE),
            simulatePlayerTurn(true,bossHitpoints,bossDamage,50,500,0,0,0,0, Integer.MAX_VALUE)
        );
    }

    private static int simulatePlayerTurn(boolean hardMode, int bossHitpoints, int bossDamage, int playerHitpoints, int mana, int manaSpent, int shieldTimer, int poisonTimer, int rechargeTimer, int lowestWinningManaSpent) {
        if (manaSpent >= lowestWinningManaSpent) {
            return lowestWinningManaSpent;
        }
        if (hardMode) {
            playerHitpoints--;
            if (playerHitpoints <= 0) {
                return lowestWinningManaSpent;
            }
        }
        if (shieldTimer > 0) {
            shieldTimer--;
        }
        if (poisonTimer > 0) {
            poisonTimer--;
            bossHitpoints -= POISON_DAMAGE;
            if (bossHitpoints <= 0) {
                return manaSpent;
            }
        }
        if (rechargeTimer > 0) {
            rechargeTimer--;
            mana += RECHARGE_MANA_INCREASE;
        }
        if (mana >= MAGIC_MISSILE_MANA_COST) {
            lowestWinningManaSpent = simulateBossTurn(hardMode, bossHitpoints-MAGIC_MISSILE_DAMAGE, bossDamage, playerHitpoints, mana-MAGIC_MISSILE_MANA_COST, manaSpent+MAGIC_MISSILE_MANA_COST, shieldTimer, poisonTimer, rechargeTimer, lowestWinningManaSpent);
        }
        if (mana >= DRAIN_MANA_COST) {
            lowestWinningManaSpent = simulateBossTurn(hardMode, bossHitpoints-DRAIN_TRANSFER, bossDamage, playerHitpoints+DRAIN_TRANSFER, mana-DRAIN_MANA_COST, manaSpent+DRAIN_MANA_COST, shieldTimer, poisonTimer, rechargeTimer, lowestWinningManaSpent);
        }
        if (mana >= SHIELD_MANA_COST && shieldTimer <= 0) {
            lowestWinningManaSpent = simulateBossTurn(hardMode, bossHitpoints, bossDamage, playerHitpoints, mana-SHIELD_MANA_COST, manaSpent+SHIELD_MANA_COST, SHIELD_TURNS, poisonTimer, rechargeTimer, lowestWinningManaSpent);
        }
        if (mana >= POISON_MANA_COST && poisonTimer <= 0) {
            lowestWinningManaSpent = simulateBossTurn(hardMode, bossHitpoints, bossDamage, playerHitpoints, mana-POISON_MANA_COST, manaSpent+POISON_MANA_COST, shieldTimer, POISON_TURNS, rechargeTimer, lowestWinningManaSpent);
        }
        if (mana >= RECHARGE_MANA_COST && rechargeTimer <= 0) {
            lowestWinningManaSpent = simulateBossTurn(hardMode, bossHitpoints, bossDamage, playerHitpoints, mana-RECHARGE_MANA_COST, manaSpent+RECHARGE_MANA_COST, shieldTimer, poisonTimer, RECHARGE_TURNS, lowestWinningManaSpent);
        }
        return lowestWinningManaSpent;
    }

    private static int simulateBossTurn(boolean hardMode, int bossHitpoints, int bossDamage, int playerHitpoints, int mana, int manaSpent, int shieldTimer, int poisonTimer, int rechargeTimer, int lowestWinningManaSpent) {
        if (manaSpent >= lowestWinningManaSpent) {
            return lowestWinningManaSpent;
        }
        int playerArmor = 0;
        if (shieldTimer > 0) {
            shieldTimer--;
            playerArmor = SHIELD_ARMOR_INCREASE;
        }
        if (poisonTimer > 0) {
            poisonTimer--;
            bossHitpoints -= POISON_DAMAGE;
        }
        if (rechargeTimer > 0) {
            rechargeTimer--;
            mana += RECHARGE_MANA_INCREASE;
        }
        if (bossHitpoints <= 0) {
            return manaSpent;
        }
        playerHitpoints -= Math.max(1,bossDamage-playerArmor);
        if (playerHitpoints <= 0) {
            return lowestWinningManaSpent;
        }
        return simulatePlayerTurn(hardMode, bossHitpoints, bossDamage, playerHitpoints, mana, manaSpent, shieldTimer, poisonTimer, rechargeTimer, lowestWinningManaSpent);
    }
}
