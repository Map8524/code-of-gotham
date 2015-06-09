package com.canoo.cog.ui.city.util;

import java.util.List;

import com.canoo.cog.solver.CityNode;
import com.canoo.cog.ui.city.model.Building;
import com.canoo.cog.ui.city.model.City;
import com.canoo.cog.ui.city.model.Hood;
import com.canoo.cog.ui.city.model.style.CityStyle;
import com.canoo.cog.ui.city.model.text.Info;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class StageUtil {

    private static final int SCENE_HEIGHT = 1000;

    private static final int SCENE_WIDTH = 1500;

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

    private double CAMERA_INITIAL_DISTANCE = -1200;
    private final double CAMERA_INITIAL_X_ANGLE = -30;
    private final double CAMERA_INITIAL_Y_ANGLE = 30.0;
    private final double CAMERA_NEAR_CLIP = 0.1;
    private final double CAMERA_FAR_CLIP = 10000.0;

    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();

    public void setTextProperties(Group root, City city, String title) {
        double depthCorrection = -city.getLayoutBounds().getDepth()/2;

        double heightCorrectionTitle =  -(SCENE_HEIGHT / 2 -20);
        double heightCorrectionInfo =  -SCENE_HEIGHT / 4;

        double widthCorrectionTitle =  SCENE_WIDTH / 3 -10;
        double widthCorrectionInfo =  SCENE_WIDTH / 3;

        double tileFont = Math.min(city.getLayoutBounds().getDepth() / 6, 42);
        double txtFont = Math.min(city.getLayoutBounds().getDepth() / 12, 18);

        Text titleTxt = new Text(title);
        titleTxt.setFont(Font.font("Helvetica", FontWeight.BOLD, tileFont));
        titleTxt.setFill(Color.WHITESMOKE);

        Text elementInfoTxt = new Text();
        elementInfoTxt.setFont(Font.font("Helvetica", FontWeight.BOLD, txtFont));
        elementInfoTxt.setFill(Color.WHITESMOKE);
        elementInfoTxt.textProperty().bind(Info.getInfoProperty());

        Group titleGroup = new Group();
        titleGroup.getChildren().add(titleTxt);

        Group elementInfoGroup = new Group();
        elementInfoGroup.getChildren().add(elementInfoTxt);

        root.getChildren().add(titleGroup);
        root.getChildren().add(elementInfoGroup);

        titleGroup.setTranslateY(heightCorrectionTitle);
        titleGroup.setTranslateX(widthCorrectionTitle);
//		titleGroup.setTranslateZ(depthCorrection);

        elementInfoGroup.setTranslateY(heightCorrectionInfo);
        elementInfoGroup.setTranslateX(widthCorrectionInfo);
//		elementInfoGroup.setTranslateZ(depthCorrection);

        root.getChildren().addAll(new SphereMenuBuilder().build(depthCorrection, city.getWidth()));
    }

    private SimpleStringProperty styleProperty = new SimpleStringProperty();

    public static final CityStyle.Style INITIAL_STYLE = CityStyle.Style.GOTHAM;

    public void setStyle(Scene scene) {
        // initial style
        styleProperty.bind(CityStyle.getStyleProperty());
        styleProperty.addListener(listener -> setBackgroundColor(scene));
        CityStyle.setStyle(INITIAL_STYLE);
    }

    private void setBackgroundColor(Scene scene) {
        scene.setFill(CityStyle.getBackgroundColor(styleProperty.getValue()));
    }

    public City createCity(CityNode resultNode, String cityName) {
        // Add city to group and translate them according to model
        int hoodHeight = 1;
        int potentialHeight = resultNode.getSize() / 400;
        if (potentialHeight > 1) {
            hoodHeight = potentialHeight;
        }
        City city = new City(hoodHeight, resultNode.getSize(), resultNode.getSize(), cityName);
        addAllNodesRecursively(city, resultNode.getChildren());

        LayoutManager layoutManager = new LayoutManager();
        layoutManager.moveCityChildrenBackToCity(city);
        layoutManager.correctYDirection(city);
        layoutManager.setRelativeOffset(city);
        layoutManager.moveCityToChildren(city);

        city.setTranslateX(city.getWidth() / 2);
        city.setTranslateZ(city.getWidth() / 2);
        city.setTranslateY(100);
        return city;
    }

    private static final int HEIGHT_DIVISOR = 3;

    private void addAllNodesRecursively(Hood hood, List<CityNode> children) {
        for (CityNode node : children) {

            String info = node.getModel().getInfo();
            if (node.isLeaf()) {
                double height = node.getModel().getLinesOfCode() / HEIGHT_DIVISOR;
                hood.addBuilding(new Building(node.getSize(), height, node.getX(), node.getY(), info, node.getModel(), hood.getLevel() + 1)); // Y==Z
            } else {
                double incrementedHeight = hood.getHeight();
                Hood childHood = new Hood(incrementedHeight, node.getSize(), node.getSize(), node.getX(), node.getY(), info, hood.getLevel() + 1);
                hood.addHood(childHood);
                addAllNodesRecursively(childHood, node.getChildren());
            }
        }
    }

    public void setMouseEventsToScene(Scene scene, PerspectiveCamera camera) {
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

    Rotate rxBox = new Rotate(0, 0, 0, 0, Rotate.X_AXIS);
    Rotate ryBox = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
    Rotate rzBox = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);

    public Scene initScene(City city, PerspectiveCamera camera) {
        Group root = new Group();
        buildCamera(root, camera);
        root.getTransforms().addAll(rxBox, ryBox, rzBox);
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
        scene.setCamera(camera);
        root.getChildren().add(city);
        return scene;
    }

    private void buildCamera(Group root, PerspectiveCamera camera) {
        System.out.println("buildCamera()");
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    public void makeTimeLine(PerspectiveCamera camera) {
        // Timeline for Intro
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        KeyValue kv = new KeyValue(camera.translateZProperty(), CAMERA_INITIAL_DISTANCE);
        KeyFrame kf = new KeyFrame(Duration.millis(5000), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
}
