package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.drive.MecanumDrive;
import org.firstinspires.ftc.teamcode.sensors.Gyro;

@TeleOp(name = "teleOpTest3")
public class test3 extends OpMode
{
    MecanumDrive drive = new MecanumDrive();
    Gyro gyro = new Gyro();
    DcMotor motor1;
    DcMotor motor2;
    double power;
    @Override
    public void init()
    {
        drive.init(hardwareMap);
        gyro.init(hardwareMap);
        motor1 = hardwareMap.dcMotor.get("firstSlide");
        motor2 = hardwareMap.dcMotor.get("secondSlide");
        motor2.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void loop()
    {
        double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
        double x = gamepad1.left_stick_x;
        double rx = gamepad1.right_stick_x;

        if (gamepad1.start)
        {
            gyro.ResetYaw();
        }
        if (gamepad1.a)
        {
            power = 0.5;
        }
        else if (gamepad1.b)
        {
            power = 0;
        }
        drive.runAllMotorTest(power);

        final double triggerThreshold = 0.1;

        double verticalSlidePosition = motor1.getCurrentPosition();

        double power = 0;

        if ((gamepad2.left_bumper || gamepad2.right_bumper) &&
                (gamepad2.left_trigger <= triggerThreshold ||
                        gamepad2.right_trigger <= triggerThreshold))
        {
            power = -1;
        }
        else if ((gamepad2.left_trigger > triggerThreshold ||
                gamepad2.right_trigger > triggerThreshold) &&
                !(gamepad2.left_bumper || gamepad2.right_bumper))
        {
            power = 1;
        }
        motor1.setPower(power);
        motor2.setPower(power);
//        double heading = gyro.getHeading(AngleUnit.RADIANS);
//        telemetry.addData("Heading: ", heading);
//        telemetry.update();
//   cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc     drive.setMotorDrive(x, y, rx, heading);
    }
}
