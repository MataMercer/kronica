import { useRouter } from "next/router";
import { useEffect } from "react";

export default function Callback() {
    const router = useRouter();
    const { code } = router.query;
    const { oauthLogin, loginError } = useAuth();
    useEffect(() => {
        if (code) oauthLogin(code as String);
    }, [code]);

    return (
        <div title="Logging in...">
            <div>error goes herel lol</div>
            <h1>Logging in...</h1>
            <p>Logging in with Github. You will be redirected shortly...</p>
        </div>
    );
}
