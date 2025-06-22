"use client";
import { User, UserRole } from "../Types/Models";
import useCurrentUser from "../hooks/useCurrentUser";

interface AuthProtectionProps extends React.PropsWithChildren<{}> {
    children: React.ReactNode;
    requiredRole: keyof typeof UserRole;
}

export default function AuthProtection({
    children,
    requiredRole,
}: AuthProtectionProps) {
    const { user, loading, loggedOut, mutate } = useCurrentUser();

    function hasRequiredRole(
        user: User | undefined,
        requiredRole: keyof typeof UserRole
    ): boolean {
        if (!user) {
            return false;
        }
        const userRoleLevel = UserRole[user.role];
        const requiredRoleLevel = UserRole[requiredRole];
        return userRoleLevel >= requiredRoleLevel;
    }

    return <>{hasRequiredRole(user, requiredRole) && children}</>;
}
