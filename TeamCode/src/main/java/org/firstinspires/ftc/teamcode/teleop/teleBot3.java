package org.firstinspires.ftc.teamcode.teleop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.robot.ArmPosition;
import org.firstinspires.ftc.teamcode.robot.Claw;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.Slide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@TeleOp(name = "teleBot3")
public class teleBot3 extends OpMode
{
    private enum HorizontalSlideState
    {
        NORMAL,
        RETRACT,
        CONNECT
    }
    private enum VerticalSlideState
    {
        NORMAL,
        CONNECT,
        GRAB
    }

    HashMap<ArmPosition.Horizontal, Double> horizontalArmPositionsMap =
            new HashMap<ArmPosition.Horizontal, Double>();
    HashMap<ArmPosition.Vertical, Double> verticalArmPositionsMap =
            new HashMap<ArmPosition.Vertical, Double>();

    private ArmPosition.Horizontal horizontalArmPosition = ArmPosition.Horizontal.CONNECTING_POSITION;
    ArrayList<ArmPosition.Vertical> verticalArmPositions =
            new ArrayList<ArmPosition.Vertical>();

    final int verticalSlideUpperLimit = 1900;
    final int verticalSlideLowerLimit = 0;
    final int horizontalSlideUpperLimit = 600;
    final int horizontalSlideLowerLimit = 0;

    final double verticalMotorSpeed = 1.0;
    final double horizontalMotorSpeed = 1.0;

    private VerticalSlideState verticalSlideState = VerticalSlideState.NORMAL;
    private HorizontalSlideState horizontalSlideState = HorizontalSlideState.NORMAL;

    private int verticalArmPositionIndex = 0;

    private boolean aButtonPressed = false;
    private boolean bButtonPressed = false;
    private boolean xButtonPressed = false;
    private boolean yButtonPressed = false;

    private double currentHorizontalSlidePosition;
    private double currentVerticalSlidePosition;

    Robot robot;
    Gamepad drivingGamepad;

    @Override
    public void init() {
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.CONNECTING_POSITION, 1.0);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.BARRIER_AVOIDING_POSITION, 0.45);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.AIMING_POSITION, 0.38);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.GRABBING_POSITION, 0.0);

        verticalArmPositionsMap.put(ArmPosition.Vertical.CONNECTING_POSITION, 0.0);
        verticalArmPositionsMap.put(ArmPosition.Vertical.DUMPING_POSITION, 0.85);
        verticalArmPositionsMap.put(ArmPosition.Vertical.SPECIMEN_POSITION, 1.0);

        // Have to add it manually because it messes up the order
        // verticalArmPositions.addAll(verticalArmPositionsMap.keySet());
        verticalArmPositions.add(0, ArmPosition.Vertical.CONNECTING_POSITION);
        verticalArmPositions.add(1, ArmPosition.Vertical.DUMPING_POSITION);
        verticalArmPositions.add(2, ArmPosition.Vertical.SPECIMEN_POSITION);

        robot = new Robot(horizontalArmPositionsMap, verticalArmPositionsMap);
        robot.init(hardwareMap);

        robot.horizontalClawOpenPosition = 0.75;
        robot.horizontalClawClosePosition = 0;
        robot.verticalClawOpenPosition = 0.5;
        robot.verticalClawClosePosition = 0;
    }

    @Override
    public void loop()
    {
        driveManualHandler();

        if (gamepad1.y)
        {
            horizontalSlideState = HorizontalSlideState.RETRACT;
        }

        switch (horizontalSlideState)
        {
            case NORMAL:
                horizontalSlideManualHandler();
                horizontalClawManualHandler();
                horizontalArmManualHandler();
                break;
            case RETRACT:
                horizontalArmPosition = ArmPosition.Horizontal.BARRIER_AVOIDING_POSITION;
                robot.SetArmPosition(horizontalArmPosition);
                currentHorizontalSlidePosition = -robot.GetSlidesPosition(Slide.HORIZONTAL);
                if (currentHorizontalSlidePosition > horizontalSlideLowerLimit)
                {
                    robot.SetSlidesMotorPower(Slide.HORIZONTAL, horizontalMotorSpeed);
                }
                else
                {
                    horizontalArmPosition = ArmPosition.Horizontal.CONNECTING_POSITION;
                    robot.SetArmPosition(horizontalArmPosition);
                    robot.SetSlidesMotorPower(Slide.HORIZONTAL, 0);
                    horizontalSlideState = HorizontalSlideState.NORMAL;
                }
        }
        switch (verticalSlideState)
        {
            case NORMAL:
                verticalArmManualHandler();
                verticalClawManualHandler();
                verticalSlideManualHandler();
            case CONNECT:
        }
    }

    private void driveManualHandler()
    {
        switchManualHandler();
        if (drivingGamepad == null) return;
        double forward = -drivingGamepad.left_stick_y;
        double turn = drivingGamepad.right_stick_x / 2;
        robot.drive.setMotorDrivePowers(forward - turn, forward + turn);
    }
    private void switchManualHandler()
    {
        if ((gamepad1.left_stick_button && gamepad1.right_stick_button) || (gamepad2.back))
        {
            drivingGamepad = gamepad1;
        } else if ((gamepad2.left_stick_button && gamepad2.right_stick_button) || (gamepad1.back))
        {
            drivingGamepad = gamepad2;
        }
    }
    private void verticalArmManualHandler()
    {
        if (gamepad2.a && !aButtonPressed)
        {
            if (verticalArmPositionIndex > 0)
            {
                verticalArmPositionIndex--;
            }
            aButtonPressed = true;
        }
        else if (!gamepad2.a)
        {
            aButtonPressed = false;
        }

        if (gamepad2.b && !bButtonPressed)
        {
            if (verticalArmPositionIndex < 2)
            {
                verticalArmPositionIndex++;
            }
            bButtonPressed = true;
        }
        else if (!gamepad2.b)
        {
            bButtonPressed = false;
        }
        robot.SetArmPosition(verticalArmPositions.get(verticalArmPositionIndex));
    }
    private void horizontalArmManualHandler()
    {
        if (gamepad1.dpad_down)
        {
            horizontalArmPosition = ArmPosition.Horizontal.GRABBING_POSITION;
        }
        else if (gamepad1.dpad_up)
        {
            horizontalArmPosition = ArmPosition.Horizontal.CONNECTING_POSITION;
        }
        else if (gamepad1.dpad_right)
        {
            horizontalArmPosition = ArmPosition.Horizontal.AIMING_POSITION;
        }
        else if (gamepad1.dpad_left)
        {
            horizontalArmPosition = ArmPosition.Horizontal.BARRIER_AVOIDING_POSITION;
        }
        robot.SetArmPosition(horizontalArmPosition);
    }
    private void horizontalClawManualHandler()
    {
        if (gamepad1.x && !xButtonPressed)
        {
            robot.SetClawPosition(Claw.HORIZONTAL, !robot.GetClawPosition(Claw.HORIZONTAL));
            xButtonPressed = true;
        }
        else if (!gamepad1.x)
        {
            xButtonPressed = false;
        }
    }
    private void verticalClawManualHandler()
    {
        if (gamepad2.y && !yButtonPressed)
        {
            robot.SetClawPosition(Claw.VERTICAL, !robot.GetClawPosition(Claw.VERTICAL));
            yButtonPressed = true;
        }
        else if (!gamepad2.y)
        {
            yButtonPressed = false;
        }
    }
    private void horizontalSlideManualHandler()
    {
        final double triggerThreshold = 0.1;

        currentHorizontalSlidePosition = -robot.GetSlidesPosition(Slide.HORIZONTAL);

        double power = 0;

        if ((gamepad1.right_bumper || gamepad1.left_bumper) &&
                (gamepad1.right_trigger <= triggerThreshold ||
                        gamepad1.left_trigger <= triggerThreshold))
        {
            if (currentHorizontalSlidePosition > horizontalSlideLowerLimit)
            {
                power = horizontalMotorSpeed;
            }
        }
        else if ((gamepad1.right_trigger > triggerThreshold ||
                gamepad1.left_trigger > triggerThreshold) &&
                (!gamepad1.right_bumper || !gamepad1.left_bumper))
        {
            if (currentHorizontalSlidePosition < horizontalSlideUpperLimit)
            {
                power = -horizontalMotorSpeed;
            }
        }
        robot.SetSlidesMotorPower(Slide.HORIZONTAL, power);
    }
    private void verticalSlideManualHandler()
    {
        final double triggerThreshold = 0.1;

        currentVerticalSlidePosition = robot.GetSlidesPosition(Slide.VERTICAL);

        double power = 0;

        if ((gamepad2.left_bumper || gamepad2.right_bumper) &&
                (gamepad2.left_trigger <= triggerThreshold ||
                        gamepad2.right_trigger <= triggerThreshold))
        {
            if (currentVerticalSlidePosition > verticalSlideLowerLimit)
            {
                power = -verticalMotorSpeed;
            }
        }
        else if ((gamepad2.left_trigger > triggerThreshold ||
                gamepad2.right_trigger > triggerThreshold) &&
                !(gamepad2.left_bumper || gamepad2.right_bumper))
        {
            if (currentVerticalSlidePosition < verticalSlideUpperLimit)
            {
                power = verticalMotorSpeed;
            }
        }
        robot.SetSlidesMotorPower(Slide.VERTICAL, power);
    }
}