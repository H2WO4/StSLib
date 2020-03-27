package com.evacipated.cardcrawl.mod.stslib.patches.cardInterfaces;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.BranchingUpgradesCard;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.exordium.LivingWall;
import com.megacrit.cardcrawl.events.shrines.AccursedBlacksmith;
import com.megacrit.cardcrawl.events.shrines.Designer;
import com.megacrit.cardcrawl.events.shrines.UpgradeShrine;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.neow.NeowReward;
import com.megacrit.cardcrawl.screens.select.GridCardSelectScreen;
import com.megacrit.cardcrawl.ui.buttons.GridSelectConfirmButton;
import com.megacrit.cardcrawl.vfx.campfire.CampfireSmithEffect;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class BranchingUpgradesPatch {

    @SpirePatch(
            clz = AbstractCard.class,
            method = SpirePatch.CLASS
    )
    public static class BranchingUpgradeField {
        public static SpireField<Boolean> isBranchUpgraded = new SpireField<>(() -> false);
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = SpirePatch.CLASS
    )
    public static class BranchSelectFields {
        //BranchSelectFields
        public static SpireField<AbstractCard> branchUpgradePreviewCard = new SpireField<>(() -> null);
        //WaitingForBranchUpgradeSelection
        public static SpireField<Boolean> waitingForBranchUpgradeSelection = new SpireField<>(() -> false);
        //IsBranchUpgrading
        public static SpireField<Boolean> isBranchUpgrading = new SpireField<>(() -> false);
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = "update"
    )
    public static class GetBranchingUpgrade {
        @SpireInsertPatch(
                locator = Locator.class
        )
        public static void Insert(GridCardSelectScreen __instance) {
            AbstractCard c = getHoveredCard();
            if (c instanceof BranchingUpgradesCard) {
                AbstractCard previewCard = c.makeStatEquivalentCopy();
                BranchingUpgradesCard setBranchUpgradesCard = (BranchingUpgradesCard) previewCard;
                setBranchUpgradesCard.setIsBranchUpgrade();
                setBranchUpgradesCard.displayBranchUpgrades();
                BranchSelectFields.branchUpgradePreviewCard.set(__instance, previewCard);
                BranchSelectFields.waitingForBranchUpgradeSelection.set(__instance, true);
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "makeStatEquivalentCopy");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
            }
        }
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = "update"
    )
    public static class StupidFuckingUpdateBullshitImSoMadDontChangeThisClassNameKio {
        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(GridCardSelectScreen __instance) {
            if (BranchSelectFields.branchUpgradePreviewCard.get(__instance) != null) {
                BranchSelectFields.branchUpgradePreviewCard.get(__instance).update();
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "update");
                return new int[]{LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher)[1]};
            }
        }
    }

    public static ArrayList<AbstractCard> cardList = new ArrayList<>();

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = "render"
    )
    public static class RenderBranchingUpgrade {
        // Instrument to insert the patch call after renderArrows()
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getMethodName().equals("renderArrows")) {
                        m.replace("$_ = $proceed($$);" +
                                "if (" + RenderBranchingUpgrade.class.getName() + ".Do(this, sb).isPresent()) {" +
                                "return;" +
                                "}");
                    }
                }
            };
        }
        public static SpireReturn Do(GridCardSelectScreen __instance, SpriteBatch sb) {
            AbstractCard c = getHoveredCard();
            if (__instance.forUpgrade && c instanceof BranchingUpgradesCard) {
                cardList.clear();
                AbstractCard branchUpgradedCard = BranchSelectFields.branchUpgradePreviewCard.get(__instance);
                c.current_x = (Settings.WIDTH * 0.36F);
                c.current_y = (Settings.HEIGHT / 2.0F);
                c.target_x = (Settings.WIDTH * 0.36F);
                c.target_y = (Settings.HEIGHT / 2.0F);
                c.render(sb);
                c.updateHoverLogic();
                c.hb.resize(0, 0);
                if (__instance.upgradePreviewCard.hb.hovered) {
                    __instance.upgradePreviewCard.drawScale = 1;
                } else {
                    __instance.upgradePreviewCard.drawScale = 0.9F;
                }
                __instance.upgradePreviewCard.current_x = (Settings.WIDTH * 0.63F);
                __instance.upgradePreviewCard.current_y = (Settings.HEIGHT * 0.75F - (50 * Settings.scale));
                __instance.upgradePreviewCard.target_x = (Settings.WIDTH * 0.63F);
                __instance.upgradePreviewCard.target_y = (Settings.HEIGHT * 0.75F - (50 * Settings.scale));
                __instance.upgradePreviewCard.render(sb);
                __instance.upgradePreviewCard.updateHoverLogic();
                __instance.upgradePreviewCard.renderCardTip(sb);
                cardList.add(__instance.upgradePreviewCard);
                if (branchUpgradedCard.hb.hovered) {
                    branchUpgradedCard.drawScale = 1;
                } else {
                    branchUpgradedCard.drawScale = 0.9F;
                }
                branchUpgradedCard.current_x = (Settings.WIDTH * 0.63F);
                branchUpgradedCard.current_y = (Settings.HEIGHT / 4.0F + (50 * Settings.scale));
                branchUpgradedCard.target_x = (Settings.WIDTH * 0.63F);
                branchUpgradedCard.target_y = (Settings.HEIGHT / 4.0F + (50 * Settings.scale));
                branchUpgradedCard.render(sb);
                branchUpgradedCard.updateHoverLogic();
                branchUpgradedCard.renderCardTip(sb);
                cardList.add(branchUpgradedCard);
                if ((__instance.forUpgrade) || (__instance.forTransform) || (__instance.forPurge) || (__instance.isJustForConfirming) || (__instance.anyNumber)) {
                    __instance.confirmButton.render(sb);
                }
                CardGroup targetGroup = (CardGroup) ReflectionHacks.getPrivate(__instance, GridCardSelectScreen.class, "targetGroup");
                String tipMsg = (String) ReflectionHacks.getPrivate(__instance, GridCardSelectScreen.class, "tipMsg");
                if ((!__instance.isJustForConfirming) || (targetGroup.size() > 5)) {
                    FontHelper.renderDeckViewTip(sb, tipMsg, 96.0F * Settings.scale, Settings.CREAM_COLOR);
                }
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }


    @SpirePatch(
            clz = GridSelectConfirmButton.class,
            method = "render"
    )
    public static class BranchUpgradeConfirm {
        public static SpireReturn Prefix(GridSelectConfirmButton __instance, SpriteBatch sb) {
            AbstractCard c = getHoveredCard();
            if (BranchSelectFields.waitingForBranchUpgradeSelection.get(AbstractDungeon.gridSelectScreen) && c instanceof BranchingUpgradesCard) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = "renderArrows"
    )
    public static class RenderSplitArrows {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                private int count = 0;
                @Override
                public void edit(MethodCall m) throws CannotCompileException
                {
                    if (m.getClassName().equals(SpriteBatch.class.getName()) && m.getMethodName().equals("draw")) {
                        if (count != 0) {
                            m.replace("if (forUpgrade && hoveredCard instanceof " + BranchingUpgradesCard.class.getName() + ") {" +
                                    "$10 = 45f;" +
                                    "$3 += 64f * " + Settings.class.getName() + ".scale *" + count + ";" +
                                    "$_ = $proceed($$);" +
                                    "$10 = -45f;" +
                                    "$3 -= 2 * 64f * " + Settings.class.getName() + ".scale *" + count + ";" +
                                    "}" +
                                    "$_ = $proceed($$);");
                        }
                        ++count;
                    }
                }
            };
        }
    }

    @SpirePatch(
            clz = GridCardSelectScreen.class,
            method = "cancelUpgrade"
    )
    public static class CancelUpgrade {
        public static void Prefix(GridCardSelectScreen __instance) {
            BranchSelectFields.waitingForBranchUpgradeSelection.set(__instance, false);
            BranchSelectFields.isBranchUpgrading.set(__instance, false);
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "update"
    )
    public static class SelectBranchedUpgrade {
        public static void Postfix(AbstractCard __instance) {
            if (AbstractDungeon.screen == AbstractDungeon.CurrentScreen.GRID && AbstractDungeon.gridSelectScreen.forUpgrade) {
                if (__instance.hb.hovered && InputHelper.justClickedLeft) {
                    if (BranchingUpgradeField.isBranchUpgraded.get(__instance)) {
                        BranchSelectFields.isBranchUpgrading.set(AbstractDungeon.gridSelectScreen, true);
                    } else {
                        BranchSelectFields.isBranchUpgrading.set(AbstractDungeon.gridSelectScreen, false);
                    }

                    if (__instance instanceof BranchingUpgradesCard) {
                        __instance.beginGlowing();
                        cardList.forEach(c -> {
                            if (c != __instance) c.stopGlowing();
                        });
                    }

                    BranchSelectFields.waitingForBranchUpgradeSelection.set(AbstractDungeon.gridSelectScreen, false);
                }
            }
        }
    }

    @SpirePatch(
            clz = CampfireSmithEffect.class,
            method = "update"
    )
    @SpirePatch(
            clz = NeowReward.class,
            method = "update"
    )
    @SpirePatch(
            clz = UpgradeShrine.class,
            method = "update"
    )
    @SpirePatch(
            clz = AccursedBlacksmith.class,
            method = "update"
    )
    @SpirePatch(
            clz = LivingWall.class,
            method = "update"
    )
    @SpirePatch(
            clz = Designer.class,
            method = "update"
    )
    public static class DoBranchUpgrade {
        public static ExprEditor Instrument() {
            return new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getClassName().equals(AbstractCard.class.getName()) && m.getMethodName().equals("upgrade")) {
                        m.replace(
                                "if (((Boolean)" + BranchSelectFields.class.getName() + ".isBranchUpgrading.get(" + AbstractDungeon.class.getName() + ".gridSelectScreen)).booleanValue()) {" +
                                        "if ($0 instanceof " + BranchingUpgradesCard.class.getName() + ") {" +
                                        "((" + BranchingUpgradesCard.class.getName() + ")$0).setIsBranchUpgrade();" +
                                        "}" +
                                        BranchSelectFields.class.getName() + ".isBranchUpgrading.set(" + AbstractDungeon.class.getName() + ".gridSelectScreen, ($w)false);" +
                                        "} else {" +
                                        "$proceed($$);" +
                                        "}"
                        );
                    }
                }
            };
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "makeStatEquivalentCopy"
    )
    public static class CopiesRetainBranchUpgrade {
        @SpireInsertPatch(
                locator = Locator.class,
                localvars = {"card"}
        )
        public static void Insert(AbstractCard __instance, AbstractCard card) {
            if (__instance.timesUpgraded < 0 && card instanceof BranchingUpgradesCard) {
                for (int i = 0; i > __instance.timesUpgraded; i--) {
                    BranchingUpgradesCard c = (BranchingUpgradesCard) card;
                    c.setIsBranchUpgrade();
                    c.setBranchDescription();
                }
            }
        }

        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(AbstractCard.class, "name");
                return LineFinder.findAllInOrder(ctBehavior, finalMatcher);
            }
        }
    }

    @SpirePatch(
            clz = CardLibrary.class,
            method = "getCopy",
            paramtypez = {
                    String.class,
                    int.class,
                    int.class
            }
    )
    public static class SaveBranchingUpgrades {
        public static AbstractCard Postfix(AbstractCard __result, String useless0, int upgradeCount, int useless1) {
            if (upgradeCount < 0 && __result instanceof BranchingUpgradesCard) {
                for (int i = 0; i > upgradeCount; i--) {
                    BranchingUpgradesCard c = (BranchingUpgradesCard) __result;
                    c.setIsBranchUpgrade();
                }
            }
            return __result;
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "upgradeName"
    )
    public static class AvoidSomeFractalsOrSomethingIGuess {
        public static void Postfix(AbstractCard __instance) {
            if (__instance instanceof BranchingUpgradesCard  && BranchingUpgradeField.isBranchUpgraded.get(__instance)) {
                __instance.timesUpgraded -= 2;
                String tmp = __instance.name.substring(__instance.name.length()-1);
                if(tmp.equals("+")) {
                    __instance.name  =__instance.name.substring(0, __instance.name.length()-1) + "*";
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "getRewardCards"
    )
    public static class BranchUpgradeRewards {
        public static ArrayList<AbstractCard> Postfix(ArrayList<AbstractCard> __result) {
            for (int i = 0; i <__result.size(); ++i) {
                AbstractCard c = __result.get(i);
                if (c instanceof BranchingUpgradesCard && c.upgraded) {
                    AbstractCard copy = c.makeCopy();
                    BranchingUpgradesCard branchCopy = (BranchingUpgradesCard) copy;
                    if (AbstractDungeon.cardRng.randomBoolean(branchCopy.getBranchUpgradeRewardChance())) {
                        branchCopy.setIsBranchUpgrade();
                        __result.set(i, copy);
                    }
                }
            }
            return __result;
        }
    }

    public static Field hoveredCardField;
    public static AbstractCard getHoveredCard() {
        GridCardSelectScreen gc = AbstractDungeon.gridSelectScreen;
        try {
            if (hoveredCardField == null) {
                hoveredCardField = gc.getClass().getDeclaredField("hoveredCard");
            }
            hoveredCardField.setAccessible(true);
            return (AbstractCard) hoveredCardField.get(gc);
        } catch (Exception e) {
            System.out.println("Exception occurred when getting private field hoveredCard from StSLib: " + e.toString());
            return null;
        }
    }
}