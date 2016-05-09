package com.gluonapplication;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import javafx.animation.FadeTransition;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchPoint;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class BasicView extends View {
    HashMap<Integer, TouchNode> touches = new HashMap<>();
    Pane pane = new Pane();

    public BasicView(String name) {
        super(name);

        setCenter(pane);

        //hide app bar
        this.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = MobileApplication.getInstance().getAppBar();
                appBar.setVisible(false);
            }
        });

        zoomCircle.setStrokeWidth(5);
        zoomCircle.setStroke(Color.GAINSBORO);
        longline.setStrokeWidth(5);

        //correct position for touch events on HIDPI screens
        final double HIDPI = Screen.getPrimary().getBounds().getHeight()/Screen.getPrimary().getVisualBounds().getHeight();

        pane.setOnTouchPressed(event -> {
            TouchPoint p = event.getTouchPoint();
            TouchNode n = new TouchNode(p.getId(), p.getSceneX() * HIDPI - pane.getLayoutX(), p.getSceneY() * HIDPI - pane.getLayoutY());
            //TouchNode n = new TouchNode(p.getId(), p.getX(), p.getY());

            touches.put(p.getId(), n);
            pane.getChildren().add(n);

            System.out.println("touch press started");
            event.consume();
        });
        pane.setOnTouchMoved(event -> {
            TouchPoint p = event.getTouchPoint();
            TouchNode n = touches.get(p.getId());
            n.setLayoutX(p.getSceneX() * HIDPI - this.getLayoutX());
            //n.setLayoutX(p.getX());
            n.setLayoutY(p.getSceneY() * HIDPI - this.getLayoutY());
            //n.setLayoutY(p.getY());
            n.getCircle().setStroke(Color.VIOLET);

            System.out.println("touch moved");
            event.consume();
        });
        pane.setOnTouchStationary(event -> {
            TouchPoint p = event.getTouchPoint();
            TouchNode n = touches.get(p.getId());
            n.getCircle().setStroke(Color.AQUA);

            System.out.println("touch stationary");
            event.consume();
        });

        pane.setOnTouchReleased(event -> {
            for(Node n:pane.getChildren()) {
                if(n instanceof TouchNode && ((TouchNode)n).getTouchID() == event.getTouchPoint().getId()) {
                    pane.getChildren().remove(n);
                    break;
                }
            }
            System.out.println("touch released");
            event.consume();
        });


        //swipe
        pane.setOnSwipeUp(event -> {paintSwipeUI(event.getSceneX(), event.getSceneY(), Side.TOP, 100);
            System.out.println("swipe up"); event.consume();});
        pane.setOnSwipeLeft(event -> {paintSwipeUI(event.getSceneX(), event.getSceneY(), Side.LEFT, 100);
            System.out.println("swipe left"); event.consume();});
        pane.setOnSwipeRight(event -> {paintSwipeUI(event.getSceneX(), event.getSceneY(), Side.RIGHT, 100);
            System.out.println("swipe right"); event.consume();});
        pane.setOnSwipeDown(event -> {paintSwipeUI(event.getSceneX(), event.getSceneY(), Side.BOTTOM, 100);
            System.out.println("swipe down"); event.consume();});


        //rotation
        pane.setOnRotationStarted(event -> {
            if (!(pane.getChildren().contains(rec)))
                pane.getChildren().add(rec);
            System.out.println("rotation started");
            event.consume();
        });
        pane.setOnRotationFinished(event -> {
            if (pane.getChildren().contains(rec))
                pane.getChildren().remove(rec);
            System.out.println("rotation finished");
            event.consume();
        });
        pane.setOnRotate(event -> {
            RotateUI(event);
            System.out.println("rotating");
            event.consume();
        });

        //zoom
        pane.setOnZoomStarted(event -> {
            if (!(pane.getChildren().contains(zoomCircle)))
                pane.getChildren().add(zoomCircle);
            System.out.println("zoom started");
            event.consume();
        });
        pane.setOnZoomFinished(event -> {
            if (pane.getChildren().contains(zoomCircle))
                pane.getChildren().remove(zoomCircle);
            System.out.println("zoom finished");
            event.consume();
        });
        pane.setOnZoom(event -> {
            ZoomUI(event);
            System.out.println("zooming");
            event.consume();
        });

        //scrolling
        pane.setOnScrollStarted(event -> {
            pane.getChildren().removeAll(lines);
            pane.getChildren().remove(longline);
            lines.clear();
            longline.setStartX(event.getX());
            longline.setStartY(event.getY());
            pane.getChildren().add(longline);

            System.out.println("scroll started");
            event.consume();
        });
        pane.setOnScrollFinished(event -> {
            longline.setEndX(event.getX() + event.getDeltaX());
            longline.setEndY(event.getY() + event.getDeltaY());
            System.out.println("scroll finished");
            event.consume();
        });
        pane.setOnScroll(event -> {
            paintScrollUI(event);
            System.out.println("scrolling");
            event.consume();
        });


        //MOUSE
        pane.setOnMousePressed(event -> {
            Circle c = new Circle(10, Color.CORNFLOWERBLUE);
            c.setCenterX(event.getX());
            c.setCenterY(event.getY());
            pane.getChildren().add(c);
            FadeTransition ft = new FadeTransition(new Duration(1000), c);
            ft.setOnFinished(event1 -> {
                if (pane.getChildren().contains(c)) {
                    pane.getChildren().remove(c);
                    c.setDisable(true);
                }
            });
            ft.play();
            System.out.println("mouse pressed");
            event.consume();
        });
    }


    int size_rec = 50;
    Rectangle rec = new Rectangle(size_rec, size_rec, Color.ORANGE);

    public void RotateUI(RotateEvent e) {
        rec.setX(e.getSceneX() - this.getLayoutX() - size_rec / 2);
        rec.setY(e.getSceneY() - this.getLayoutY() - size_rec / 2);
        rec.setRotate(e.getTotalAngle());
    }


    Line longline = new Line();
    Collection<Line> lines = new LinkedList<>();

    private void paintScrollUI(ScrollEvent e) {
        Line line = new Line(e.getX(), e.getY(), e.getX() + e.getDeltaX(), e.getY() + e.getDeltaY());
        lines.add(line);
        pane.getChildren().add(line);

        //set position of long line
        longline.setEndX(e.getX());
        longline.setEndY(e.getY());
    }


    public void paintSwipeUI(double x, double y, Side side, double length) {
        double xnew = x;
        double ynew = y;
        switch (side) {
            case TOP:
                ynew -= length;
                break;
            case BOTTOM:
                ynew += length;
                break;
            case LEFT:
                xnew -= length;
                break;
            case RIGHT:
                xnew += length;
                break;
        }
        Line line = new Line(x, y, xnew, ynew);
        line.setStrokeWidth(5);
        line.setStroke(Color.ORANGE);
        pane.getChildren().add(line);
        Circle c = new Circle(x, y, 10, Color.ORANGE);
        pane.getChildren().add(c);
        FadeTransition ft = new FadeTransition(new Duration(1000), line);
        ft.setOnFinished(event1 -> {
            if (pane.getChildren().contains(line)) {
                pane.getChildren().remove(line);
            }
            if (pane.getChildren().contains(c)) {
                pane.getChildren().remove(c);
            }
        });
        ft.play();
    }


    Circle zoomCircle = new Circle(80, Color.TRANSPARENT);

    public void ZoomUI(ZoomEvent e) {
        zoomCircle.setScaleX(e.getTotalZoomFactor());
        zoomCircle.setScaleY(e.getTotalZoomFactor());
        zoomCircle.setCenterX(e.getX());
        zoomCircle.setCenterY(e.getY());
    }


}

/*
    UI component to display touches
 */
class TouchNode extends Group {
    public Circle getCircle() {
        return circle;
    }

    private final Circle circle;

    public Label getLabel() {
        return label;
    }


    private final Label label;
    private final int touchID;

    public TouchNode(int id, double x, double y) {
        super();
        this.touchID = id;
        circle = new Circle(40, Color.TRANSPARENT);
        circle.setStroke(Color.VIOLET);
        circle.setStrokeWidth(10);
        label = new Label("ID: " + id);
        label.setTranslateX(40);
        label.setTranslateY(40);
        super.getChildren().add(circle);
        super.getChildren().add(label);
        super.setLayoutX(x);
        super.setLayoutY(y);
    }

    public int getTouchID() {
        return touchID;
    }
}
