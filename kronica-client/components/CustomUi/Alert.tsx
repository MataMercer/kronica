type Props = {
    message: string;
};

export default function Alert({ message }: Props) {
    return <div>{message}</div>;
}
