import { fetchCurrentUser } from "./fetch/auth";

export default async function AuthProtectionSSR({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const currentUser = await fetchCurrentUser();
    const loggedOut = !!currentUser.id;
    return <>{!!loggedOut && children}</>;
}
