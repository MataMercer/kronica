import { fetchCurrentUser } from "../fetch/auth";
import { UserRole } from "../Types/Models";
import { hasRequiredRole } from "./Utils";

interface AuthProtectionSSRProps extends React.PropsWithChildren<{}> {
    children: React.ReactNode;
    requiredRole: keyof typeof UserRole;
}

export default async function AuthProtectionSSR({
    children,
    requiredRole,
}: AuthProtectionSSRProps) {
    const currentUser = await fetchCurrentUser();
    const loggedOut = !!currentUser.id;
    return <>{hasRequiredRole(currentUser, requiredRole) && children}</>;
}
