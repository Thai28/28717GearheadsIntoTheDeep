package org.firstinspires.ftc.teamcode.auton;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;


@Autonomous(name = "firstAuton")
public class firstAuton extends LinearOpMode {
    private DcMotor leftDrive;
    private DcMotor rightDrive;
    private DcMotor verticalSlideMotor; // Motor for the vertical slide
    private Servo armServo;
    private Servo verticalClawServo;

    @Override
    public void runOpMode() {
        // Initialize the hardware variables
        leftDrive = hardwareMap.get(DcMotor.class, "left_motor");
        rightDrive = hardwareMap.get(DcMotor.class, "right_motor");
        leftDrive.setDirection(DcMotorSimple.Direction.REVERSE);
        verticalSlideMotor = hardwareMap.get(DcMotor.class, "verticalSlide"); // Initialize vertical slide motor
        armServo = hardwareMap.get(Servo.class, "verticalArm");
        verticalClawServo = hardwareMap.servo.get("verticalClaw");

        // Set the arm position to 1.0

        verticalClawServo.setPosition(0);

        // Wait for the start signal
        waitForStart();

        // Move vertical slide motor to position 1000
        armServo.setPosition(1.0);
        moveVerticalSlideToPosition(800);

        sleep(1500);

        // Move forward
        moveForward(1.0, 550); // Move forward at full power for 1000 milliseconds

        sleep(1500);

        verticalClawServo.setPosition(0.5);
        moveVerticalSlideToPosition(500);

        sleep(1500);
        moveForward(-1.0, 350);

        sleep(500);
        moveVerticalSlideToPosition(0);

        sleep(500);
        armServo.setPosition(0);
        sleep(500);
    }

    private void moveVerticalSlideToPosition(int targetPosition) {
        // Set the target position for the vertical slide motor
        verticalSlideMotor.setTargetPosition(targetPosition);
        verticalSlideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // Set power to the motor
        verticalSlideMotor.setPower(1.0); // Full power

        // Wait until the motor reaches the target position
        while (opModeIsActive() && verticalSlideMotor.isBusy()) {
            // Optionally, you can add telemetry here to monitor the position
            telemetry.addData("Vertical Slide Position", verticalSlideMotor.getCurrentPosition());
            telemetry.update();
        }

        // Stop the motor after reaching the target position
        verticalSlideMotor.setPower(0);
        verticalSlideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER); // Reset to using encoder
    }

    private void moveForward(double power, long duration) {
        // Set the power to the motors
        leftDrive.setPower(power);
        rightDrive.setPower(power);

        // Wait for the specified duration
        sleep(duration);

        // Stop the motors
        leftDrive.setPower(0);
        rightDrive.setPower(0);
    }
}