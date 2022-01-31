package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import com.qualcomm.robotcore.hardware.ColorSensor;


@Autonomous(name="NewHashIsABadSpelerAuto")
public class NewHashIsABadSpelerAuto extends LinearOpMode {

    enum DriveDirection {
        FORWARD,
        LEFT,
        RIGHT,
        BACKWARD
    }

    enum StartingPositionEnum {
        BLUESTORAGEUNIT,
        BLUEWAREHOUSE,
        REDSTORAGEUNIT,
        REDWAREHOUSE
    }

    enum SlidePackDirection {
        UP,
        DOWN
    }

    enum ShippingHubLevel {
        TOP,
        MIDDLE,
        BOTTOM
    }

    private final StartingPositionEnum STARTING_POSITION = StartingPositionEnum.REDWAREHOUSE;
    private final double BATTERY_LEVEL = 1;
    private final double DrivePower = 0.75;
    private final double SlidePackPower = 0.5;
    private final double GrabberLGrabPosition = 0.55;
    private final double GrabberLReleasePosition = 0.25;
    private final double GrabberRGrabPosition = 0.3;
    private final double GrabberRReleasePosition = 0.6;

    private DcMotor FLMotor;
    private DcMotor FRMotor;
    private DcMotor BLMotor;
    private DcMotor BRMotor;
    private DcMotor Flywheel;
    private CRServo GrabberL;
    private CRServo GrabberR;
    private DcMotor VerticalSlidePack;
    // private DcMotor HorizontalSlidePack;
    // private DcMotor VerticalSlidePack;
    // private DcMotor EaterMotor;
    private Blinker expansion_Hub_4;
    DigitalChannel touch;
    BNO055IMU imu;
    private Orientation lastAngles = new Orientation();
    double globalAngle, power = .30, correction;
    private ElapsedTime eTime = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException
    {
        //Maps hardware for all parts
        FLMotor = hardwareMap.dcMotor.get("1");
        FRMotor = hardwareMap.dcMotor.get("0");
        BLMotor = hardwareMap.dcMotor.get("2");
        BRMotor = hardwareMap.dcMotor.get("3");
        Flywheel = hardwareMap.dcMotor.get("Fly");
        GrabberL = hardwareMap.crservo.get("GL");
        GrabberR = hardwareMap.crservo.get("GR");
        VerticalSlidePack = hardwareMap.dcMotor.get("VSP");
        VerticalSlidePack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // HorizontalSlidePack = hardwareMap.dcMotor.get("HorizontalSlidePack");
        // VerticalSlidePack = hardwareMap.dcMotor.get("VerticalSlidePack");
        // EaterMotor = hardwareMap.dcMotor.get("Eater");

        FLMotor.setDirection(DcMotor.Direction.REVERSE);
        FRMotor.setDirection(DcMotor.Direction.FORWARD);
        BLMotor.setDirection(DcMotor.Direction.REVERSE);
        BRMotor.setDirection(DcMotor.Direction.FORWARD);
        Flywheel.setDirection(DcMotor.Direction.FORWARD);
        GrabberL.setDirection(CRServo.Direction.FORWARD);
        GrabberR.setDirection(CRServo.Direction.REVERSE);
        VerticalSlidePack.setDirection(DcMotor.Direction.FORWARD);
        // HorizontalSlidePack.setDirection(DcMotor.Direction.FORWARD);
        // VerticalSlidePack.setDirection(DcMotor.Direction.FORWARD);
        // EaterMotor.setDirection(DcMotor.Direction.FORWARD);

        waitForStart();
        telemetry.addData("Status","Auto");
        telemetry.update();

        //Configures settings for different parts
        FLMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BLMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        FRMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        BRMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        Flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        VerticalSlidePack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // HorizontalSlidePack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // VerticalSlidePack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        // EaterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // FLMotor.setMode(DcMotor.RunMode.RUN);
        // BRMotor.setMode(DcMotor.RunMode.RUN);
        // FRMotor.setMode(DcMotor.RunMode.RUN);
        // BLMotor.setMode(DcMotor.RunMode.RUN);
        // Flywheel.setMode(DcMotor.RunMode.RUN);
        // HorizontalSlidePack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // VerticalSlidePack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // EaterMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        telemetry.addData("Mode", "waiting for start");
        telemetry.update();

        // wait for start button.
        waitForStart();

        telemetry.addData("Mode", "running");
        telemetry.update();

        sleep(50);
        //The actual program
        eTime.reset();

        pickUpBlock(getCameraReading());

        switch (STARTING_POSITION) {
            case REDSTORAGEUNIT:
                doStorageUnitActions(StartingPositionEnum.REDSTORAGEUNIT);
                break;
            case REDWAREHOUSE:
                doWarehouseActions(StartingPositionEnum.REDWAREHOUSE, true);
//                drive(DriveDirection.FORWARD, DrivePower, 700);
//                sleep(500); // For Testing Purposes
//                drive(DriveDirection.RIGHT, DrivePower, 700);
//                sleep(500);
//                drive(DriveDirection.FORWARD, .9, 1500);
                break;
            case BLUESTORAGEUNIT:
                doStorageUnitActions(StartingPositionEnum.BLUESTORAGEUNIT);
                break;
            case BLUEWAREHOUSE:
                doWarehouseActions(StartingPositionEnum.BLUEWAREHOUSE, true);
//                drive(DriveDirection.FORWARD, DrivePower, 700);
//                sleep(500); // For Testing Purposes
//                drive(DriveDirection.LEFT, DrivePower, 700);
//                sleep(500);
//                drive(DriveDirection.FORWARD, .9, 1500);
                break;
            default:
                break;
        }
    }

    private ShippingHubLevel getCameraReading() {
        return ShippingHubLevel.BOTTOM;
    }

    private DriveDirection getCorrectDirection(DriveDirection direction, boolean needInvert) {
        if (!needInvert)
            return direction;

        DriveDirection invertedDirection = direction;
        switch (direction) {
            case LEFT:
                invertedDirection = DriveDirection.RIGHT;
                break;
            case RIGHT:
                invertedDirection = DriveDirection.LEFT;
                break;
            case FORWARD:
                invertedDirection = DriveDirection.BACKWARD;
                break;
            case BACKWARD:
                invertedDirection = DriveDirection.FORWARD;
                break;
            default:
                break;
        }

        return invertedDirection;
    }

    private int convertShippingHubLevelToMs(ShippingHubLevel shl) {
        switch (shl) {
            case TOP:
                return 800;
            case MIDDLE:
                return 500;
            case BOTTOM:
                return 450;
        }
        return 200;
    }

    private void pickUpBlock(ShippingHubLevel shl) {
        moveSlidePack(SlidePackDirection.UP, -getDrivePower(SlidePackPower), 500);
        sleep(200);

        openClaw();
        sleep(200);

        // Step 1: Drive Forward (Push Block Forward)
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 190);
        sleep(200);

        // Step 2: Drive Backward
        drive(DriveDirection.BACKWARD, getDrivePower(DrivePower), 180);
        sleep(200);

        // Step 3: Move Slide Pack Down
        moveSlidePack(SlidePackDirection.DOWN, -getDrivePower(SlidePackPower), 500);
        sleep(200);

        // // Step 4: Grab Block
        closeClaw();
        sleep(800);

        // // Step 5: Move Slide Pack Up
        moveSlidePack(SlidePackDirection.UP, -getDrivePower(SlidePackPower), convertShippingHubLevelToMs(shl));
        sleep(200);
    }

    private void doWarehouseActions(StartingPositionEnum position, boolean normal) {
        boolean needInvert = (position != StartingPositionEnum.BLUEWAREHOUSE);

        // Step 1: Forward
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 1300);
        sleep(500);

        // Step 1.5: Turn toward carousel
        drive(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 650);
        sleep(500);

        // Step 2: Forward
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 230);
        sleep(1000);

        // Step 3: Drop Block
        openClaw();
        sleep(2000);

        // Step 4: Backward
        drive(DriveDirection.BACKWARD, getDrivePower(DrivePower), 210);
        sleep(1000);

        closeClaw();
        sleep(1000);

        if (normal) {
            // ALTERNATIVELY: Step 5: Strafe Right
            strafe(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 1400);
            strafe(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(0.3), 1900);
            sleep(500);


            // Step 5.5: Re-align by Strafing Left
            strafe(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(0.3), 200);
            sleep(500);

            // Step 6: Backward
            drive(DriveDirection.BACKWARD, getDrivePower(DrivePower), 1100);
            sleep(500);

            // Step 7: Strafe Left
            strafe(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(DrivePower), 1100);
            sleep(1000);


            // Step 7.5: Turn Right
            drive(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 620);
            sleep(500);

            // Step 8: Strafe Right
            strafe(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 800);
            sleep(500);

        } else {
            // Step 5: Strafe Right
            strafe(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 600);
            sleep(1000);

            // Step 6: Backward
            drive(DriveDirection.BACKWARD, getDrivePower(DrivePower), 2000);
        }
    }

    private void doStorageUnitActions(StartingPositionEnum position) {
        boolean needInvert = (position != StartingPositionEnum.BLUESTORAGEUNIT);

        // Step 0: Grip Block Tightly
        GrabberL.getController().setServoPosition(GrabberL.getPortNumber(), GrabberLGrabPosition);
        GrabberR.getController().setServoPosition(GrabberR.getPortNumber(), GrabberRGrabPosition);

        // Step 1: Forward
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 650);
        sleep(500);

        // Step 2: Left 45
        drive(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(DrivePower), 380);
        sleep(500);

        // Step 3: Forward
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 450);
        sleep(1000);

        // Step 4: Drop Block
        openClaw();
        sleep(2000);


        // UNUSED! Step 5: Turn Left Slightly
        // drive(DriveDirection.LEFT, DrivePower, );

        // Step 6: Backward to Carousel
        if (position == StartingPositionEnum.BLUESTORAGEUNIT) {
            drive(DriveDirection.LEFT, getDrivePower(DrivePower), 75);
        } else {
            drive(DriveDirection.RIGHT, getDrivePower(DrivePower), 65);
        }
        sleep(500);

        drive(DriveDirection.BACKWARD, getDrivePower(DrivePower), 1000);

//        if (position == StartingPositionEnum.BLUESTORAGEUNIT) {
//            drive(DriveDirection.LEFT, getDrivePower(DrivePower), 200);
//        }
//
//        drive(DriveDirection.BACKWARD, getDrivePower(0.15), 2400);
        drive(DriveDirection.BACKWARD, getDrivePower(0.15), 2200);
        sleep(2000);

        // Step 7: Spin Carousel
        if (needInvert)
            spinFlywheel(-0.3, 5000);
        else
            spinFlywheel(0.3, 5000);

        sleep(500);

        closeClaw();
        sleep(1000);

        // Step 8: Forward
        drive(DriveDirection.FORWARD, getDrivePower(DrivePower), 600);

        // Step 9: Turn Left Slightly
        drive(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(DrivePower), 300);

        // Step 10: Strafe to Wall
        strafe(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(DrivePower), 500);
        strafe(getCorrectDirection(DriveDirection.LEFT, needInvert), getDrivePower(0.3), 1500);
        sleep(500);

        // Step 11: Strafe to Center
        strafe(getCorrectDirection(DriveDirection.RIGHT, needInvert), getDrivePower(DrivePower), 1150);

        // Step 12: Backward to Storage Unit
        drive(DriveDirection.BACKWARD, getDrivePower(0.4), 1350);
    }

    private void openClaw() {
        GrabberL.getController().setServoPosition(GrabberL.getPortNumber(), GrabberLReleasePosition);
        GrabberR.getController().setServoPosition(GrabberR.getPortNumber(), GrabberRReleasePosition);
    }

    private void closeClaw() {
        GrabberL.getController().setServoPosition(GrabberL.getPortNumber(), GrabberLGrabPosition);
        GrabberR.getController().setServoPosition(GrabberR.getPortNumber(), GrabberRGrabPosition);
    }

    private int getDriveTime(int time) {
        return (int) (time * (1 + (0.1 - BATTERY_LEVEL * 0.1)));
    }

    private double getDrivePower(double power) {
        return power * (1 + (0.1 - BATTERY_LEVEL * 0.1));
    }

    private void spinFlywheel(double power, double time) {
        eTime.reset();
        Flywheel.setPower(power);
        while(opModeIsActive() && eTime.milliseconds() < time){
            telemetry.addData("Time:", eTime);
            telemetry.update();
        }
        Flywheel.setPower(0);
    }

    private void drive(DriveDirection direction, double power, double time) {
        eTime.reset();
        switch(direction) {
            case FORWARD:
                FLMotor.setPower(power);
                FRMotor.setPower(power);
                BLMotor.setPower(power);
                BRMotor.setPower(power);
                break;
            case LEFT:
                FLMotor.setPower(-power);
                FRMotor.setPower(power);
                BLMotor.setPower(-power);
                BRMotor.setPower(power);
                break;
            case RIGHT:
                FLMotor.setPower(power);
                FRMotor.setPower(-power);
                BLMotor.setPower(power);
                BRMotor.setPower(-power);
                break;
            case BACKWARD:
                FLMotor.setPower(-power);
                FRMotor.setPower(-power);
                BLMotor.setPower(-power);
                BRMotor.setPower(-power);
        }
        while(opModeIsActive() && eTime.milliseconds() < time){
            telemetry.addData("Time:", eTime);
            telemetry.update();
        }
        FLMotor.setPower(0);
        FRMotor.setPower(0);
        BLMotor.setPower(0);
        BRMotor.setPower(0);
    }

    private void moveSlidePack(SlidePackDirection spd, double power, double time) {
        eTime.reset();
        switch(spd) {
            case UP:
                VerticalSlidePack.setPower(power);
                break;
            case DOWN:
                VerticalSlidePack.setPower(-power);
                break;
        }
        while(opModeIsActive() && eTime.milliseconds() < time){
            telemetry.addData("Time:", eTime);
            telemetry.update();
        }
        VerticalSlidePack.setPower(0);
    }

    private void timedrive(double power, double time){
        eTime.reset();
        while(opModeIsActive() && eTime.milliseconds() < time){
            FLMotor.setPower(power);
            FRMotor.setPower(power);
            BLMotor.setPower(power);
            BRMotor.setPower(power);
            telemetry.addData("Time:", eTime);
            telemetry.update();
        }
        FLMotor.setPower(0);
        FRMotor.setPower(0);
        BLMotor.setPower(0);
        BRMotor.setPower(0);
    }
    private void strafe(DriveDirection driveDirection, double power, double time){
        eTime.reset();
        switch (driveDirection) {
            case LEFT:
                while(opModeIsActive() && eTime.milliseconds() < time){
                    FLMotor.setPower(power);
                    FRMotor.setPower(power);
                    BLMotor.setPower(-power);
                    BRMotor.setPower(-power);
                    telemetry.addData("Time:", eTime);
                    telemetry.update();
                }
                break;
            case RIGHT:
                while(opModeIsActive() && eTime.milliseconds() < time){
                    FLMotor.setPower(-power);
                    FRMotor.setPower(-power);
                    BLMotor.setPower(power);
                    BRMotor.setPower(power);
                    telemetry.addData("Time:", eTime);
                    telemetry.update();
                }
                break;
        }
        FLMotor.setPower(0);
        FRMotor.setPower(0);
        BLMotor.setPower(0);
        BRMotor.setPower(0);
    }
    private void leftturn(double power, double time){
        eTime.reset();
        while(opModeIsActive() && eTime.milliseconds() < time){
            FLMotor.setPower(-1 * power);
            FRMotor.setPower(1 * power);
            BLMotor.setPower(-1 * power);
            BRMotor.setPower(1 * power);
        }
        FLMotor.setPower(0);
        FRMotor.setPower(0);
        BLMotor.setPower(0);
        BRMotor.setPower(0);
    }
    private void rightturn(double power, double time){
        eTime.reset();
        while(opModeIsActive() && eTime.milliseconds() < time){
            FLMotor.setPower(-1 * power);
            FRMotor.setPower(1 * power);
            BLMotor.setPower(-1 * power);
            BRMotor.setPower(1 *power);
        }
        FLMotor.setPower(0);
        FRMotor.setPower(0);
        BLMotor.setPower(0);
        BRMotor.setPower(0);
    }




    /*(gamepad2.a){
            LGServo.setPosition(0);
            RGServo.setPosition(0.75);
        }else if (gamepad2.b){
            LGServo.setPosition(0.75);
            RGServo.setPosition(0);
        }
    */
    private void resetAngle()
    {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    //Get Angle Function
    private double getAngle()
    {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    //    Check Direction Function
    private double checkDirection()
    {
        double correction, angle, gain = .10;

        angle = getAngle();

        if (angle == 0)
            correction = 0;
        else
            correction = -angle;

        correction = correction * gain;

        return correction;
    }

    //Rotate Function
    private void rotate(double power, int degrees)
    {
        if(opModeIsActive())
        {
            double currentAng = 0;
            resetAngle();
            if (power > 0)
            {
                while(opModeIsActive() && (-currentAng) < degrees)
                {
                    currentAng  =   getAngle();
                    FLMotor.setPower(power);
                    FRMotor.setPower(-power);
                    BLMotor.setPower(power);
                    BRMotor.setPower(-power);

                    telemetry.addData("Angle: ", currentAng);
                    telemetry.addData("Target: ", degrees);
                }
            }

            else
            {
                degrees = -degrees;
                while(opModeIsActive() && (currentAng) < degrees)
                {
                    currentAng  =   getAngle();
                    FLMotor.setPower(power);
                    FRMotor.setPower(-power);
                    BLMotor.setPower(power);
                    BRMotor.setPower(-power);

                    telemetry.addData("Angle: ", currentAng);
                    telemetry.addData("Target: ", degrees);
                }
            }

            FLMotor.setPower(0);
            FRMotor.setPower(0);
            BLMotor.setPower(0);
            BRMotor.setPower(0);

        }
    }

    //Encoder Drive Function
    // public void drive(double power, int centimeters)
    // {

    //     int currentPosFR    = 0;
    //     int currentPosFL    = 0;
    //     int currentPosBR    = 0;
    //     int currentPosBL    = 0;
    //     int target = centimeters * 45;

    //     // Ensure that the opmode is still active
    //     if (opModeIsActive())
    //     {
    //         FRMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    //         FLMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    //         BLMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    //         BRMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);

    //         // Turn On RUN_TO_POSITION
    //         FLMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    //         BRMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    //         FRMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    //         BLMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

    //         // Determine new target position, and pass to motor controller
    //         BRMotor.setTargetPosition(target);
    //         FLMotor.setTargetPosition(target);
    //         BLMotor.setTargetPosition(target);
    //         FRMotor.setTargetPosition(target);

    //         FLMotor.setPower(power);
    //         BRMotor.setPower(power);
    //         FRMotor.setPower(power);
    //         BLMotor.setPower(power);

    //         // keep looping while we are still active, and there is time left, and both motors are running.
    //         // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
    //         // its target position, the motion will stop.  This is "safer" in the event that the robot will
    //         // always end the motion as soon as possible.
    //         // However, if you require that BOTH motors have finished their moves before the robot continues
    //         // onto the next step, use (isBusy() || isBusy()) in the loop test.
    //         if (power > 0)
    //         {
    //             while (opModeIsActive() && (currentPosFL < target || currentPosBR < target ||
    //             currentPosFR < target || currentPosBL < target))
    //             {
    //                 currentPosBR    = BRMotor.getCurrentPosition();
    //                 currentPosFL    = FLMotor.getCurrentPosition();
    //                 currentPosFR    = FRMotor.getCurrentPosition();
    //                 currentPosBL    = BLMotor.getCurrentPosition();

    //                 // Display it for the driver.
    //                 telemetry.addData("Running to ", target);
    //                 telemetry.addData("Running at front left: ",  currentPosFL);
    //                 telemetry.addData("Running at back right: ", currentPosBR);
    //                 telemetry.addData("Running at front right: ",  currentPosFR);
    //                 telemetry.addData("Running at back left: ", currentPosBL);
    //                 telemetry.update();
    //             }
    //         }

    //         else
    //         {
    //             while (opModeIsActive() && (currentPosFL > target || currentPosBR > target ||
    //             currentPosFR > target || currentPosBL > target))
    //             {
    //                 currentPosBR    = BRMotor.getCurrentPosition();
    //                 currentPosFL    = FLMotor.getCurrentPosition();
    //                 currentPosFR    = FRMotor.getCurrentPosition();
    //                 currentPosBL    = BLMotor.getCurrentPosition();

    //                 // Display it for the driver.
    //                 telemetry.addData("Running to ", target);
    //                 telemetry.addData("Running at front left: ",  currentPosFL);
    //                 telemetry.addData("Running at back right: ", currentPosBR);
    //                 telemetry.addData("Running at front right: ",  currentPosFR);
    //                 telemetry.addData("Running at back left: ", currentPosBL);
    //                 telemetry.update();
    //             }
    //         }
    //     }

//        // Stop all motion;
//        BRMotor.setPower(0);
//        FRMotor.setPower(0);
//        FLMotor.setPower(0);
//        BLMotor.setPower(0);
//
//        // Turn off RUN_TO_POSITION
//        FLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        BRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        FRMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//        BLMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    //  sleep(250);   // optional pause after each move


//Land Function
}
