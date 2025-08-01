"use client";
import { useRouter } from "next/navigation";
import { useForm, SubmitHandler } from "react-hook-form";
import { refreshCurrentUser } from "../actions";
import Link from "next/link";
import useCurrentUser from "../hooks/useCurrentUser";
type Inputs = {
    email: string;
    password: string;
};
export default function LoginForm() {
    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<Inputs>();

    const {
        user: currentUser,
        loading,
        loggedOut,
        mutate: mutateCurrentUser,
    } = useCurrentUser();

    const router = useRouter();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const response = await fetch("http://localhost:7070/api/auth/login", {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: data.email,
                password: data.password,
            }),
        });
        refreshCurrentUser();
        mutateCurrentUser();
        router.push("/home");
    };

    return (
        <div className="flex flex-col items-center justify-between p-24 space-y-2">
            <form
                className="flex flex-col items-center space-y-4"
                onSubmit={handleSubmit(onSubmit)}
            >
                <label className="flex flex-col">
                    EMAIL
                    <input
                        {...register("email", { required: true })}
                        defaultValue={"example@gmail.com"}
                    />
                </label>
                <label className="flex flex-col">
                    PASSWORD
                    <input
                        {...register("password", { required: true })}
                        defaultValue={"password"}
                    />
                    {errors.password && <span>This field is required</span>}
                </label>
                <button type="submit" className="button">
                    SUBMIT
                </button>
                <div className="m-5 border-black border-b-2 w-[100%] text-center">
                    OR
                </div>
            </form>

            <Link href="http://localhost:7070/api/oauth/discord/login">
                <button className="button">LOGIN WITH DISCORD</button>
            </Link>
        </div>
    );
}
