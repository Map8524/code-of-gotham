package com.canoo.cog.ui.city;

/*
 * #%L
 * code-of-gotham
 * %%
 * Copyright (C) 2015 Canoo Engineering AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;

import com.canoo.cog.solver.CityNode;
import com.canoo.cog.ui.city.model.Building;
import com.canoo.cog.ui.city.model.City;
import com.canoo.cog.ui.city.model.Hood;
import com.canoo.cog.ui.city.util.StageUtil;
import com.canoo.cog.ui.city.util.Xform;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class CityBuilder {


    private static final int SCENE_HEIGHT = 1000;

    private static final int SCENE_WIDTH = 1500;

    private String title = "";

    private final StageUtil stageUtil = new StageUtil();

    private SimpleStringProperty styleProperty = new SimpleStringProperty();

    public String getTitle() {
        return title;
    }

    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private double CAMERA_INITIAL_DISTANCE = -1200;
    private final double CAMERA_INITIAL_X_ANGLE = -30;
    private final double CAMERA_INITIAL_Y_ANGLE = 30.0;
    private final double CAMERA_NEAR_CLIP = 0.1;
    private final double CAMERA_FAR_CLIP = 10000.0;
    private final double CONTROL_MULTIPLIER = 0.1;
    private final double SHIFT_MULTIPLIER = 10.0;
    private final double MOUSE_SPEED = 0.1;
    private final double ROTATION_SPEED = 1.0;
    private final double TRACK_SPEED = 0.3;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;
    Rotate rxBox = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
    Rotate ryBox = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
    Rotate rzBox = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);

    private Scene scene;

    public Scene build(CityNode resultNode, String cityName) {

        City city = stageUtil.createCity(resultNode, cityName);

        // Create root group and scene
        Group root = new Group();
        root.getTransforms().addAll(rxBox, ryBox, rzBox);
        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
        scene.setCamera(camera);


        title = city.getInfo();
        root.getChildren().add(city);

        // Add mouse events
        setMouseEventsToScene(scene);

        // Timeline for Intro
        buildCamera(root, city);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        KeyValue kv = new KeyValue(camera.translateZProperty(), CAMERA_INITIAL_DISTANCE);
        KeyFrame kf = new KeyFrame(Duration.millis(5000), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();

        stageUtil.setTextProperties(root, city, title);
        stageUtil.setStyle(scene);


        return scene;
    }



    private void buildCamera(Group root, City city) {
        System.out.println("buildCamera()");
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
//        cameraXform3.setRotateZ(0.0);
//        cameraXform3.setRotateY(0.0);
//        cameraXform3.setRotateX(0.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        // camera.setTranslateX(city.getWidth()/2);
        // camera.setTranslateZ(city.getWidth()/2);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void setMouseEventsToScene(Scene scene) {
        scene.setOnMousePressed(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        scene.setOnScroll(event -> {
            if (event.getDeltaY() < 0) {
                CAMERA_INITIAL_DISTANCE *= 1.1;
            } else {
                CAMERA_INITIAL_DISTANCE *= 0.9;
            }
            camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        });

        scene.setOnMouseDragged(me -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 1.0;

            if (me.isControlDown()) {
                modifier = CONTROL_MULTIPLIER;
            }
            if (me.isShiftDown()) {
                modifier = SHIFT_MULTIPLIER;
            }
            if (me.isPrimaryButtonDown()) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
            }
        });
    }
}
