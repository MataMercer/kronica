import { CircleAlert } from "lucide-react";

type Props = {
    message: string;
};

export default function Alert({ message }: Props) {
    return (
        <div className="flex space-x-1 text-red-600 p-2">
            <CircleAlert />
            <span>Error: {message}</span>
        </div>
    );
}
