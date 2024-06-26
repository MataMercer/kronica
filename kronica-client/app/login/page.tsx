"use client";
import { useRouter } from "next/navigation";
import { useForm, SubmitHandler } from "react-hook-form";
import { refreshCurrentUser } from "../actions";
type Inputs = {
    email: string;
    password: string;
};
export default function LoginPage() {
    const {
        register,
        handleSubmit,
        watch,
        formState: { errors },
    } = useForm<Inputs>();

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
        router.push("/");
        console.log(response);
    };
    return (
        <main className="flex flex-col items-center justify-between p-24">
            <h1 className="text-2xl">LOGIN</h1>
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
            </form>
        </main>
    );
}
