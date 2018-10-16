package io.michelfaria.chrono.logic;

import com.badlogic.gdx.math.Rectangle;
import io.michelfaria.chrono.actor.CollisionEntity;

public class CollisionEntityMover {

    private CollisionEntity entity;

    public CollisionEntityMover(CollisionEntity entity) {
        this.entity = entity;
    }

    /**
     * Moves the entity by the specified numbers.
     * If the entity moved successfully, it will return true.
     */
    public boolean moveBy(float xMoveSpeed, float yMoveSpeed) {
        Rectangle futureX = new Rectangle(entity.getX() + xMoveSpeed, entity.getY(), entity.getWidth(), entity.getHeight());
        if (entity.getCollisionContext().collisionChecker.collides(entity, futureX)) {
            xMoveSpeed = 0;
        }
        Rectangle futureY = new Rectangle(entity.getX(), entity.getY() + yMoveSpeed, entity.getWidth(), entity.getHeight());
        if (entity.getCollisionContext().collisionChecker.collides(entity, futureY)) {
            yMoveSpeed = 0;
        }
        if (xMoveSpeed != 0 || yMoveSpeed != 0) {
            entity.setX(entity.getX() + xMoveSpeed);
            entity.setY(entity.getY() + yMoveSpeed);
            return true;
        }
        return false;
    }
}
