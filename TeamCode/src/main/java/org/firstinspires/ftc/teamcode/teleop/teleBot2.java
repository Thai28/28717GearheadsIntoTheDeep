package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.TankDrive;

@TeleOp(name = "teleBot2")
public class teleBot2 extends OpMode
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
        GRAB,
        EXTEND,
        DUMPING,
        RETRACT,
        OPEN
    }

    private HorizontalSlideState horizontalSlideState = HorizontalSlideState.NORMAL;
    private VerticalSlideState verticalSlideState = VerticalSlideState.NORMAL;

    DcMotor horizontalSlideMotor;
    DcMotor verticalSlideMotor;

    Servo verticalArmServo;
    Servo horizontalArmServo;

    Servo horizontalClawServo;
    Servo verticalClawServo;

    int verticalSlidePosition;
    int horizontalSlidePosition;

    int verticalArmPositionIndex = 2;

    final double verticalMotorSpeed = 1.0;
    final double horizontalMotorSpeed = 1.0;

    final int verticalSlideUpperLimit = 1900;
    final int verticalSlideLowerLimit = 0;
    final int horizontalSlideUpperLimit = 600;
    final int horizontalSlideLowerLimit = 0;

    boolean horizontalClawToggle = false;
    boolean verticalClawToggle = false;

    boolean aButtonPressed = false;
    boolean bButtonPressed = false;
    boolean previousAutomaticArmReturn = false;

    double[] verticalArmPositions = {1, 0.85, 0};
    double horizontalArmPosition = 1;

    TankDrive drive = new TankDrive();
    Gamepad drivingGamepad;

    ElapsedTime verticalTimer = new ElapsedTime();
    boolean isVerticalScoring = false;

    @Override
    public void init()
    {
        horizontalSlideMotor = hardwareMap.dcMotor.get("horizontalSlide");
        verticalSlideMotor = hardwareMap.dcMotor.get("verticalSlide");

        horizontalSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        verticalSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        verticalSlideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        verticalArmServo = hardwareMap.servo.get("verticalArm");
        horizontalArmServo = hardwareMap.servo.get("horizontalArm");

        horizontalClawServo = hardwareMap.servo.get("horizontalClaw");
        verticalClawServo = hardwareMap.servo.get("verticalClaw");

        drive.init(hardwareMap);
    }
    @Override
    public void loop()
    {
        armHandler();
        driveHandler();
        telemtryHandler();
        if (gamepad1.y)
        {
            verticalSlideState = VerticalSlideState.OPEN;
            horizontalSlideState = HorizontalSlideState.RETRACT;
        }
        if (gamepad2.x && !isVerticalScoring)
        {
            verticalTimer.reset();
            verticalSlideState = VerticalSlideState.CONNECT;
        }
        if (gamepad2.dpad_up && !isVerticalScoring)
        {
            isVerticalScoring = true;
            verticalSlideState = VerticalSlideState.EXTEND;
        }
        if (gamepad2.back)
        {
            verticalSlideState = VerticalSlideState.NORMAL;
        }
        switch (horizontalSlideState)
        {
            case NORMAL:
                horizontalSlideHandler();
                horizontalClawHandler();
                break;
            case RETRACT:
                horizontalArmPosition = 0.5;
                horizontalArmServo.setPosition(horizontalArmPosition);
                horizontalSlidePosition = -horizontalSlideMotor.getCurrentPosition();
                if (horizontalSlidePosition > horizontalSlideLowerLimit)
                {
                    horizontalSlideMotor.setPower(horizontalMotorSpeed);
                }
                if (horizontalSlidePosition <= horizontalSlideLowerLimit)
                {
                    horizontalArmPosition = 1;
                    horizontalArmServo.setPosition(horizontalArmPosition);
                    horizontalSlideMotor.setPower(0); // Return to normal state
                    horizontalSlideState = HorizontalSlideState.NORMAL;
                }
                break;
            case CONNECT:
                horizontalClawServo.setPosition(0.75);
                horizontalSlideState = HorizontalSlideState.NORMAL;
                break;
        }
        switch (verticalSlideState)
        {
            case NORMAL:
                verticalSlideHandler();
                verticalClawHandler();
                break;
            case CONNECT:
                if (verticalArmPositionIndex == 2 && !previousAutomaticArmReturn)
                {
                    verticalSlideState = VerticalSlideState.GRAB;
                    break;
                }
                else {
                    verticalArmPositionIndex = 2;
                    verticalArmServo.setPosition(verticalArmPositions[verticalArmPositionIndex]);
                    previousAutomaticArmReturn = true;
                    if (verticalTimer.milliseconds() > 650)
                    {
                        verticalSlideState = VerticalSlideState.GRAB;
                    }
                }
                break;
            case GRAB:
                verticalClawServo.setPosition(0);
                previousAutomaticArmReturn = false;
                horizontalSlideState = HorizontalSlideState.CONNECT;
                verticalSlideState = VerticalSlideState.NORMAL;
                break;
            case EXTEND:
                verticalSlidePosition = verticalSlideMotor.getCurrentPosition();
                if (verticalSlidePosition < verticalSlideUpperLimit)
                {
                    verticalSlideMotor.setPower(verticalMotorSpeed);
                }
                if (verticalSlidePosition >= verticalSlideUpperLimit)
                {
                    verticalSlideMotor.setPower(0);
                    verticalTimer.reset();
                    verticalSlideState = VerticalSlideState.DUMPING;
                }
                break;
            case DUMPING:
                verticalArmPositionIndex = 1;
                verticalArmServo.setPosition(verticalArmPositions[verticalArmPositionIndex]);
                if (verticalTimer.milliseconds() > 650)
                {
                    verticalClawServo.setPosition(0.5);
                    verticalTimer.reset();
                    verticalSlideState = VerticalSlideState.RETRACT;
                }
                break;
            case RETRACT:
                if (verticalTimer.milliseconds() < 750) break;
                verticalArmPositionIndex = 2;
                verticalArmServo.setPosition(verticalArmPositions[verticalArmPositionIndex]);
                verticalSlidePosition = verticalSlideMotor.getCurrentPosition();
                if (verticalSlidePosition > verticalSlideLowerLimit)
                {
                    verticalSlideMotor.setPower(-verticalMotorSpeed);
                }
                if (verticalSlidePosition <= verticalSlideLowerLimit)
                {
                    verticalSlideMotor.setPower(0);
                    verticalSlideState = VerticalSlideState.NORMAL;
                }
                isVerticalScoring = false;
                break;
            case OPEN:
                verticalClawServo.setPosition(0.5);
                verticalSlideState = VerticalSlideState.NORMAL;
        }
    }
    private void telemtryHandler()
    {
        telemetry.addData("Vertical slide position: ", verticalSlidePosition);
        telemetry.addData("Horizontal slide position: ", horizontalSlidePosition);
        telemetry.addData("Vertical arm position: ", verticalArmPositions[verticalArmPositionIndex]);
        telemetry.addData("Horizontal arm position: ", horizontalArmPosition);
        telemetry.addData("Vertical claw position: ", verticalClawServo.getPosition());
        telemetry.addData("Horizontal claw position: ", horizontalClawServo.getPosition());
        telemetry.addData("Driving game pad: ", drivingGamepad);
        telemetry.addData("Horizontal slide state: ", horizontalSlideState);
        telemetry.addData("Vertical slide state: ", verticalSlideState);
        telemetry.update();
    }
    private void driveHandler()
    {
        switchHandler();
        if (drivingGamepad == null) return;
        double forward = -drivingGamepad.left_stick_y;
        double turn = drivingGamepad.right_stick_x;
        drive.setMotorDrivePowers(forward - turn, forward + turn);
    }
    private void switchHandler()
    {
        if (gamepad1.left_stick_button && gamepad1.right_stick_button)
        {
            drivingGamepad = gamepad1;
        }
        else if (gamepad2.left_stick_button && gamepad2.right_stick_button)
        {
            drivingGamepad = gamepad2;
        }
    }
    private void armHandler()
    {
        verticalArmHandler();
        horizontalArmHandler();
    }
    private void verticalArmHandler()
    {
        if (gamepad2.a && !aButtonPressed) {
            if (verticalArmPositionIndex > 0) {
                verticalArmPositionIndex--; // Increase the value
            }
            aButtonPressed = true; // Set the flag to indicate button A is pressed
        } else if (!gamepad2.a) {
            aButtonPressed = false; // Reset the flag when the button is released
        }

        // Check if button B is pressed to decrease the value
        if (gamepad2.b && !bButtonPressed) {
            if (verticalArmPositionIndex < 2) {
                verticalArmPositionIndex++; // Decrease the value
            }
            bButtonPressed = true; // Set the flag to indicate button B is pressed
        } else if (!gamepad2.b) {
            bButtonPressed = false; // Reset the flag when the button is released
        }
        verticalArmServo.setPosition(verticalArmPositions[verticalArmPositionIndex]);
    }
    private void horizontalArmHandler()
    {
        if (gamepad1.dpad_down)
        {
            horizontalArmPosition = 0;
        }
        else if (gamepad1.dpad_up)
        {
            horizontalArmPosition = 1;
        }
        else if (gamepad1.dpad_right)
        {
            horizontalArmPosition = .38;
        }
        else if (gamepad1.dpad_left)
        {
            horizontalArmPosition = .45;
        }
        horizontalArmServo.setPosition(horizontalArmPosition);
    }
    private void horizontalClawHandler()
    {
        if (gamepad1.x && !horizontalClawToggle)
        {
            if (horizontalClawServo.getPosition() == 0)
                horizontalClawServo.setPosition(0.75);
            else horizontalClawServo.setPosition(0);
            horizontalClawToggle = true;
        }
        else if (!gamepad1.x)
        {
            horizontalClawToggle = false;
        }
    }
    private void verticalClawHandler()
    {
        if (gamepad2.y && !verticalClawToggle)
        {
            if (verticalClawServo.getPosition() == 0)
                verticalClawServo.setPosition(0.5);
            else verticalClawServo.setPosition(0);
            verticalClawToggle = true;
        }
        else if (!gamepad2.y)
        {
            verticalClawToggle = false;
        }
    }
    private void verticalSlideHandler()
    {
        final double triggerThreshold = 0.1;

        verticalSlidePosition = verticalSlideMotor.getCurrentPosition();

        double power = 0;

        if ((gamepad2.left_bumper || gamepad2.right_bumper) &&
                (gamepad2.left_trigger <= triggerThreshold ||
                        gamepad2.right_trigger <= triggerThreshold))
        {
            if (verticalSlidePosition > verticalSlideLowerLimit)
            {
                power = -verticalMotorSpeed;
            }
        }
        else if ((gamepad2.left_trigger > triggerThreshold ||
                gamepad2.right_trigger > triggerThreshold) &&
                !(gamepad2.left_bumper || gamepad2.right_bumper))
        {
            if (verticalSlidePosition < verticalSlideUpperLimit)
            {
                power = verticalMotorSpeed;
            }
        }
        verticalSlideMotor.setPower(power);
    }
    private void horizontalSlideHandler()
    {
        final double triggerThreshold = 0.1;

        horizontalSlidePosition = -horizontalSlideMotor.getCurrentPosition();

        double power = 0;

        if ((gamepad1.right_bumper || gamepad1.left_bumper) &&
                (gamepad1.right_trigger <= triggerThreshold ||
                        gamepad1.left_trigger <= triggerThreshold))
        {
            if (horizontalSlidePosition > horizontalSlideLowerLimit)
            {
                power = horizontalMotorSpeed;
            }
        }
        else if ((gamepad1.right_trigger > triggerThreshold ||
                gamepad1.left_trigger > triggerThreshold) &&
                (!gamepad1.right_bumper || !gamepad1.left_bumper))
        {
            if (horizontalSlidePosition < horizontalSlideUpperLimit)
            {
                power = -horizontalMotorSpeed;
            }
        }
        horizontalSlideMotor.setPower(power);
    }
}