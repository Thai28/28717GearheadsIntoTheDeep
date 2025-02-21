package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.robot.Arm;
import org.firstinspires.ftc.teamcode.robot.ArmPosition;
import org.firstinspires.ftc.teamcode.robot.Claw;
import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.Slide;

import java.util.ArrayList;
import java.util.HashMap;

@TeleOp(name = "teleBot3")
public class teleBot3 extends OpMode
{
    private enum HorizontalState
    {
        NORMAL,
        RETRACT,
        CONNECT
    }
    private enum VerticalState
    {
        NORMAL,
        CONNECT,
        GRAB,
        EXTEND,
        DUMPING,
        RETRACT,
        OPEN
    }

    HashMap<ArmPosition.Horizontal, Double> horizontalArmPositionsMap =
            new HashMap<ArmPosition.Horizontal, Double>();
    HashMap<ArmPosition.Vertical, Double> verticalArmPositionsMap =
            new HashMap<ArmPosition.Vertical, Double>();

    private ArmPosition.Horizontal horizontalArmPosition = ArmPosition.Horizontal.CONNECTING_POSITION;
    ArrayList<ArmPosition.Vertical> verticalArmPositions =
            new ArrayList<ArmPosition.Vertical>();

    private String[] horizontalSlidesMotorNames;
    private String[] verticalSlidesMotorNames;

    final int verticalSlideUpperLimit = 1450;
    final int verticalSlideLowerLimit = 0;
    final int horizontalSlideUpperLimit = 600;
    final int horizontalSlideLowerLimit = 0;

    final double verticalMotorSpeed = 1.0;
    final double horizontalMotorSpeed = 1.0;

    private VerticalState verticalState = VerticalState.NORMAL;
    private HorizontalState horizontalState = HorizontalState.NORMAL;
    private int verticalArmPositionIndex = 0;

    private boolean aButtonPressed = false;
    private boolean bButtonPressed = false;
    private boolean xButtonPressed = false;
    private boolean yButtonPressed = false;

    private boolean previousAutomaticArmReturn = false;
    private boolean isVerticalScoring = false;
    private boolean isDumped = false;
    private double currentHorizontalSlidePosition;
    private double currentVerticalSlidePosition;

    ElapsedTime verticalTimer = new ElapsedTime();

    Robot robot;
    Gamepad drivingGamepad;

    boolean debug1;

    @Override
    public void init() {
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.CONNECTING_POSITION, 1.0);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.BARRIER_AVOIDING_POSITION, 0.45);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.AIMING_POSITION, 0.38);
        horizontalArmPositionsMap.put(ArmPosition.Horizontal.GRABBING_POSITION, 0.0);

        //verticalArmPositionsMap.put(ArmPosition.Vertical.CONNECTING_POSITION, 0.0);
        //verticalArmPositionsMap.put(ArmPosition.Vertical.DUMPING_POSITION, 0.85);
        //verticalArmPositionsMap.put(ArmPosition.Vertical.SPECIMEN_POSITION, 1.0);
        verticalArmPositionsMap.put(ArmPosition.Vertical.CONNECTING_POSITION, 0.815);
        verticalArmPositionsMap.put(ArmPosition.Vertical.DUMPING_POSITION, 0.225);
        verticalArmPositionsMap.put(ArmPosition.Vertical.SPECIMEN_POSITION, 0.1);
        // Have to add it manually because it messes up the order
        // verticalArmPositions.addAll(verticalArmPositionsMap.keySet());
        verticalArmPositions.add(0, ArmPosition.Vertical.CONNECTING_POSITION);
        verticalArmPositions.add(1, ArmPosition.Vertical.DUMPING_POSITION);
        verticalArmPositions.add(2, ArmPosition.Vertical.SPECIMEN_POSITION);

        verticalSlidesMotorNames = new String[]
                {
                        "firstVerticalSlideMotor",
                        "secondVerticalSlideMotor"
                };
        horizontalSlidesMotorNames = new String[]
                {
                        "horizontalSlideMotor"
                };

        robot = new Robot(horizontalArmPositionsMap, verticalArmPositionsMap,
                verticalSlidesMotorNames, horizontalSlidesMotorNames);
        robot.init(hardwareMap);

        robot.horizontalClawOpenPosition = 0;
        robot.horizontalClawClosePosition = 0.8;
        robot.verticalClawOpenPosition = 0.5;
        robot.verticalClawClosePosition = 0;
    }

    @Override
    public void loop()
    {
        driveManualHandler();
        telemtryHandler();

        if (gamepad1.y)
        {
            verticalState = VerticalState.OPEN;
            horizontalState = HorizontalState.RETRACT;
        }
        if (gamepad1.a)
        {
            horizontalState = HorizontalState.NORMAL;
        }
        if (gamepad2.x && !isVerticalScoring)
        {
            verticalTimer.reset();
            verticalState = VerticalState.CONNECT;
            horizontalState = HorizontalState.CONNECT;
        }
        if (gamepad2.dpad_up && !isVerticalScoring)
        {
            isVerticalScoring = true;
            verticalState = VerticalState.EXTEND;
        }
        if (gamepad2.dpad_left && !isVerticalScoring)
        {
            isVerticalScoring = true;
            verticalTimer.reset();
            verticalState = VerticalState.DUMPING;
        }
        if (gamepad2.dpad_down && !isVerticalScoring)
        {
            isVerticalScoring = true;
            verticalState = VerticalState.RETRACT;
        }
        if (gamepad2.a && gamepad2.b)
        {
            verticalState = VerticalState.NORMAL;
            isVerticalScoring = false;
        }

        switch (horizontalState)
        {
            case NORMAL:
                horizontalSlideManualHandler();
                horizontalClawManualHandler();
                horizontalArmManualHandler();
                break;
            case RETRACT:
                horizontalArmPosition = ArmPosition.Horizontal.BARRIER_AVOIDING_POSITION;
                robot.SetArmPosition(horizontalArmPosition);
                currentHorizontalSlidePosition = -robot.GetSlidesPosition(Slide.HORIZONTAL, 0);
                if (currentHorizontalSlidePosition > horizontalSlideLowerLimit)
                {
                    robot.SetSlidesMotorPower(Slide.HORIZONTAL, horizontalMotorSpeed);
                }
                else
                {
                    horizontalArmPosition = ArmPosition.Horizontal.CONNECTING_POSITION;
                    robot.SetArmPosition(horizontalArmPosition);
                    robot.SetSlidesMotorPower(Slide.HORIZONTAL, 0);
                    horizontalState = HorizontalState.NORMAL;
                }
                break;
            case CONNECT:
                robot.SetClawPosition(Claw.HORIZONTAL, true);
                horizontalState = HorizontalState.NORMAL;
                break;
        }
        switch (verticalState)
        {
            case NORMAL:
                verticalArmManualHandler();
                verticalClawManualHandler();
                verticalSlideManualHandler();
                break;
            case CONNECT:
                if (robot.GetVerticalArmPosition() == ArmPosition.Vertical.CONNECTING_POSITION
                        && !previousAutomaticArmReturn)
                {
                    debug1 = true;
                    verticalState = VerticalState.GRAB;
                    break;
                }
                else
                {
                    SetArmPosition(ArmPosition.Vertical.CONNECTING_POSITION);
                    previousAutomaticArmReturn = true; // No grabbing when vertical arm is returning
                    if (verticalTimer.milliseconds() > 650)
                    {
                        verticalState = VerticalState.GRAB;
                    }
                }
                break;
            case GRAB:
                robot.SetClawPosition(Claw.VERTICAL, false);
                previousAutomaticArmReturn = false;
                isVerticalScoring = false;
                horizontalState = HorizontalState.CONNECT;
                verticalState = VerticalState.NORMAL;
                break;
            case EXTEND:
                currentVerticalSlidePosition = robot.GetSlidesPosition(Slide.VERTICAL, 0);
                if (currentVerticalSlidePosition < verticalSlideUpperLimit)
                {
                    robot.SetSlidesMotorPower(Slide.VERTICAL, verticalMotorSpeed);
                }
                if (currentVerticalSlidePosition >= verticalSlideUpperLimit)
                {
                    robot.SetSlidesMotorPower(Slide.VERTICAL, 0);
                    verticalState = VerticalState.NORMAL;
                    isVerticalScoring = false;
                }
                break;
            case DUMPING:
                SetArmPosition(ArmPosition.Vertical.DUMPING_POSITION);
                if (verticalTimer.milliseconds() > 700 && !isDumped) // Dumping
                {
                    robot.SetClawPosition(Claw.VERTICAL, true);
                    verticalTimer.reset();
                    isDumped = true;
                }
                if (verticalTimer.milliseconds() > 350 && isDumped)
                {
                    SetArmPosition(ArmPosition.Vertical.CONNECTING_POSITION);
                    verticalState = VerticalState.NORMAL;
                    isDumped = false;
                }
                isVerticalScoring = false;
                break;
            case RETRACT:
                currentVerticalSlidePosition = robot.GetSlidesPosition(Slide.VERTICAL, 0);
                if (currentVerticalSlidePosition > verticalSlideLowerLimit)
                {
                    robot.SetSlidesMotorPower(Slide.VERTICAL, -verticalMotorSpeed);
                }
                if (currentVerticalSlidePosition <= verticalSlideLowerLimit)
                {
                    robot.SetSlidesMotorPower(Slide.VERTICAL, 0);
                    verticalState = VerticalState.NORMAL;
                }
                isVerticalScoring = false;
                break;
            case OPEN:
                robot.SetClawPosition(Claw.VERTICAL, true);
                verticalState = VerticalState.NORMAL;
        }
    }
    private void telemtryHandler()
    {
        telemetry.addData("Vertical slide position: ", currentVerticalSlidePosition);
        telemetry.addData("Horizontal slide position: ", currentHorizontalSlidePosition);
        telemetry.addData("Vertical arm position: ", robot.GetArmPosition(Arm.VERTICAL));
        telemetry.addData("Horizontal arm position: ", robot.GetArmPosition(Arm.HORIZONTAL));
        telemetry.addData("Vertical claw position: ", robot.GetClawPosition(Claw.VERTICAL));
        telemetry.addData("Horizontal claw position: ", robot.GetClawPosition(Claw.HORIZONTAL));
        telemetry.addData("Driving game pad: ", drivingGamepad);
        telemetry.addData("Horizontal slide state: ", horizontalState);
        telemetry.addData("Vertical slide state: ", verticalState);
        telemetry.update();
    }
    private void driveManualHandler()
    {
        switchManualHandler();
        if (drivingGamepad == null) return;
        double y = -drivingGamepad.left_stick_y; // Remember, Y stick value is reversed
        double x = drivingGamepad.left_stick_x;
        double rx = drivingGamepad.right_stick_x;
        robot.drive.setMotorDrive(x, y, rx);
    }
    private void switchManualHandler()
    {
        if ((gamepad1.left_stick_button || gamepad1.right_stick_button) || (gamepad2.back))
        {
            drivingGamepad = gamepad1;
        } else if ((gamepad2.left_stick_button || gamepad2.right_stick_button) || (gamepad1.back))
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
            robot.SetClawPosition(Claw.HORIZONTAL, !robot.IsThisClawOpen(Claw.HORIZONTAL));
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
            robot.SetClawPosition(Claw.VERTICAL, !robot.IsThisClawOpen(Claw.VERTICAL));
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

        currentHorizontalSlidePosition = -robot.GetSlidesPosition(Slide.HORIZONTAL, 0);

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

        currentVerticalSlidePosition = robot.GetSlidesPosition(Slide.VERTICAL, 0);

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
    private void SetArmPosition(ArmPosition.Vertical armPos)
    {
        robot.SetArmPosition(armPos);
        verticalArmPositionIndex = verticalArmPositions.indexOf(armPos);
    }
    private void SetArmPosition(ArmPosition.Horizontal armPos)
    {
        robot.SetArmPosition(armPos);
        horizontalArmPosition = armPos;
    }
}