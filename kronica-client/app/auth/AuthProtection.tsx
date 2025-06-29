"use client";
import { User, UserRole } from "../Types/Models";
import useCurrentUser from "../hooks/useCurrentUser";
import { hasRequiredRole } from "./Utils";

interface AuthProtectionProps extends React.PropsWithChildren<{}> {
    children: React.ReactNode;
    requiredRole: keyof typeof UserRole;
    requiredOwnerId?: number;
}

function isUserOwner(user: User, requiredOwnerId?: number) {
    if (!requiredOwnerId) {
        return true;
    }
    return user.id === requiredOwnerId;
}

export default function AuthProtection({
    children,
    requiredRole,
    requiredOwnerId,
}: AuthProtectionProps) {
    const { user, loading, loggedOut, mutate } = useCurrentUser();

    return (
        <>
            {hasRequiredRole(user, requiredRole) &&
                user &&
                isUserOwner(user, requiredOwnerId) &&
                children}
        </>
    );
}
