/*
 * Developed by Michel Faria on 10/29/18 8:32 PM.
 * Last modified 10/29/18 8:32 PM.
 * Copyright (c) 2018. All rights reserved.
 */

package io.michelfaria.chrono.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import io.michelfaria.chrono.Game;
import io.michelfaria.chrono.animation.AnimationId;
import io.michelfaria.chrono.animation.AnimationManager;
import io.michelfaria.chrono.control.Buttons;
import io.michelfaria.chrono.control.GameInput;
import io.michelfaria.chrono.interfaces.CollisionEntity;
import io.michelfaria.chrono.interfaces.Combatant;
import io.michelfaria.chrono.interfaces.Interactible;
import io.michelfaria.chrono.interfaces.Weapon;
import io.michelfaria.chrono.items.WoodenSword;
import io.michelfaria.chrono.logic.battle.CombatStats;
import io.michelfaria.chrono.logic.FloatPair;
import io.michelfaria.chrono.logic.MoveHistory;
import io.michelfaria.chrono.util.ActorUtil;

import static io.michelfaria.chrono.animation.AnimationId.*;

public abstract class PartyCharacter extends Actor implements CollisionEntity, Combatant, Disposable {

    protected Game.Context ctx;

    protected AnimationManager animationManager = new AnimationManager();

    {
        animationManager.setCurrentAnimation(IDLE_SOUTH);
    }

    protected Direction facing = Direction.SOUTH;

    protected boolean isMoving = false;
    protected boolean isRunning = false;

    protected float walkSpeed = 1f;
    protected float runSpeedMultiplier = 2f;
    protected float stateTime = 0f;

    protected MoveHistory moveHistory = new MoveHistory(getX(), getY(), MoveHistory.STANDARD_LIMIT);
    protected CombatStats combatStats = new CombatStats();
    protected Weapon weapon = new WoodenSword();

    protected GameInput.GameInputObserverAdapter gameInputObserver;

    public PartyCharacter(Game.Context ctx) {
        this.ctx = ctx;
        setWidth(16);
        setHeight(16);

        gameInputObserver = new GameInput.GameInputObserverAdapter() {
            {
                priority = 1;
            }
            @Override
            public void buttonPressed(int controller, Buttons button) {
                if (button == Buttons.A && ctx.paused.get() == 0 && isInputEnabled()) {
                    emitInteraction();
                }
            }
        };

        ctx.collisionEntities.add(this);
        ctx.party.add(this);
        ctx.combatants.add(this);
        ctx.gameInput.addObserver(gameInputObserver);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        animationManager.draw(batch, getX(), getY(), 1);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        if (isInputEnabled()) {
            handleInput(delta);
        } else if (!ctx.battleStatus.isBattling()) {
            actFollower(delta);
        }
        updateMovementAttributes();
        updateAnimation();
        moveHistory.add(getX(), getY());
    }

    protected void handleInput(float delta) {
        assert isInputEnabled();
        assert isMainCharacter();

        if (ctx.paused.get() == 0) {
            float xMoveSpeed = 0;
            float yMoveSpeed = 0;

            if (ctx.gameInput.isButtonPressed(0, Buttons.DPAD_LEFT)) {
                isMoving = true;
                xMoveSpeed = -walkSpeed;
                facing = Direction.WEST;
            }
            if (ctx.gameInput.isButtonPressed(0, Buttons.DPAD_RIGHT)) {
                isMoving = true;
                xMoveSpeed = walkSpeed;
                facing = Direction.EAST;
            }
            if (ctx.gameInput.isButtonPressed(0, Buttons.DPAD_UP)) {
                isMoving = true;
                yMoveSpeed = walkSpeed;
                facing = Direction.NORTH;
            }
            if (ctx.gameInput.isButtonPressed(0, Buttons.DPAD_DOWN)) {
                isMoving = true;
                yMoveSpeed = -walkSpeed;
                facing = Direction.SOUTH;
            }
            isRunning = ctx.gameInput.isButtonPressed(0, Buttons.B);

            if (isRunning) {
                xMoveSpeed *= runSpeedMultiplier;
                yMoveSpeed *= runSpeedMultiplier;
            }
            if (xMoveSpeed == 0 && yMoveSpeed == 0) {
                isMoving = false;
            } else {
                ctx.collisionEntityMover.moveCollisionEntityBy(this, xMoveSpeed, yMoveSpeed);
            }
        }
    }

    protected void actFollower(float delta) {
        int nextPartyMemberIndex = ctx.party.indexOf(this, true) - 1;
        assert nextPartyMemberIndex >= 0;

        PartyCharacter nextPartyMember = ctx.party.get(nextPartyMemberIndex);

        if (nextPartyMember.moveHistory.size() > 0) {
            FloatPair last = nextPartyMember.moveHistory.getLast();
            setX(last.a);
            setY(last.b);
        }

        if (getX() != moveHistory.getPrevX() || getY() != moveHistory.getPrevY()) {
            isMoving = true;
            isRunning = nextPartyMember.isRunning;
        } else {
            isMoving = false;
            isRunning = false;
        }
    }

    protected void updateMovementAttributes() {
        int acc = 0;
        if (getX() > moveHistory.getPrevX()) {
            facing = Direction.EAST;
            acc++;
        } else if (getX() < moveHistory.getPrevX()) {
            facing = Direction.WEST;
            acc++;
        }
        if (getY() > moveHistory.getPrevY()) {
            facing = Direction.NORTH;
            acc++;
        } else if (getY() < moveHistory.getPrevY()) {
            facing = Direction.SOUTH;
            acc++;
        }
        isMoving = acc > 0;

        // Party character needs to always run if they're engaged in battle
        if (isMoving && ctx.battleStatus.isBattling()) {
            isRunning = true;
        }
    }

    protected void updateAnimation() {
        if (animationManager.getCurrentAnimation() == null) {
            animationManager.setCurrentAnimation(IDLE_SOUTH);
        }

        AnimationId north;
        AnimationId south;
        AnimationId west;
        AnimationId east;

        if (isRunning && isMoving && ctx.paused.get() == 0) {
            north = RUN_NORTH;
            south = RUN_SOUTH;
            west = RUN_WEST;
            east = RUN_EAST;
        } else if (isMoving && ctx.paused.get() == 0) {
            north = WALK_NORTH;
            south = WALK_SOUTH;
            west = WALK_WEST;
            east = WALK_EAST;
        } else {
            north = IDLE_NORTH;
            south = IDLE_SOUTH;
            west = IDLE_WEST;
            east = IDLE_EAST;
        }

        switch (facing) {
            case NORTH:
                animationManager.setCurrentAnimation(north);
                break;
            case SOUTH:
                animationManager.setCurrentAnimation(south);
                break;
            case WEST:
                animationManager.setCurrentAnimation(west);
                break;
            case EAST:
                animationManager.setCurrentAnimation(east);
                break;
        }
    }

    protected void emitInteraction() {
        assert isMainCharacter();
        assert isInputEnabled();

        Rectangle interactionRegion = ActorUtil.getActorRectangle(this);
        switch (facing) {
            case NORTH:
                interactionRegion.y += getHeight() / 2;
                break;
            case SOUTH:
                interactionRegion.y -= getHeight() / 2;
                break;
            case WEST:
                interactionRegion.x -= getWidth() / 2;
                break;
            case EAST:
                interactionRegion.x += getWidth() / 2;
                break;
        }
        interactWithRegion(interactionRegion);
    }

    protected void interactWithRegion(Rectangle region) {
        assert isMainCharacter();
        assert isInputEnabled();

        Array<CollisionEntity> arr = ctx.collisionChecker.entityCollisions(this, region);
        for (CollisionEntity ent : arr) {
            if (ent instanceof Interactible) {
                Interactible in = (Interactible) ent;
                in.interact();
                break;
            }
        }
    }

    public boolean isMainCharacter() {
        return ctx.party.get(0) == this;
    }

    public boolean isInputEnabled() {
        return isMainCharacter()
                && !ctx.battleStatus.isBattling(); // The characters shouldn't be able to move while in-battle
    }

    @Override
    public int calculateAttackDamage() {
        return Math.round((combatStats.power * 4f / 3f) + (weapon.getAttackDamage() * 5f / 9f));
    }

    @Override
    public void goToBattle(BattlePoint battlePoint, Runnable done) {
        ctx.battleStatus.setBattleGroup(battlePoint.groupId);
        addAction(
                Actions.sequence(
                        Actions.moveTo(battlePoint.getX(), battlePoint.getY(), BattlePoint.BATTLE_MOVE_DURATION),
                        Actions.run(() -> {
                            Gdx.app.debug(PartyCharacter.class.getName(), "PartyCharacter moved to BattlePoint");
                            facing = Direction.NORTH;
                            done.run();
                        })));
    }

    @Override
    public void dispose() {
        ctx.collisionEntities.remove(this);
        ctx.party.removeValue(this, true);
        ctx.combatants.remove(this);
        ctx.gameInput.removeObserver(gameInputObserver);
    }

    @Override
    public int getId() {
        return ctx.party.indexOf(this, true);
    }

    @Override
    public boolean isCollisionEnabled() {
        return isMainCharacter();
    }

    @Override
    public CombatStats getCombatStats() {
        return combatStats;
    }
}