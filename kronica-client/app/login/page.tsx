import { redirect } from "next/navigation";
import { fetchCurrentUser } from "../fetch/auth";
import LoginForm from "../forms/LoginForm";

export default async function LoginPage() {
    const currentUser = await fetchCurrentUser();

    if (currentUser.id) {
        redirect("/home");
    }

    return (
        <main className="">
            <LoginForm />
        </main>
    );
}
