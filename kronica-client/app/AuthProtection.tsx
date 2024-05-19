import useCurrentUser from "./hooks/useCurrentUser";

export default function AuthProtection({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const { user, loading, loggedOut, mutate } = useCurrentUser();

    return <>{!loggedOut && children}</>;
}
