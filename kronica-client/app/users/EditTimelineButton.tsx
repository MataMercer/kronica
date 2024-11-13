"use client";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog";
import { Controller, SubmitHandler, useForm } from "react-hook-form";
import { useEffect, useState } from "react";
import React from "react";
import KronologizeInput, {
    OrderInput,
} from "../components/inputs/KronologizeInput";
import useCurrentUser from "../hooks/useCurrentUser";
import { refreshArticles } from "../actions";
import { fetchAllArticles } from "../fetch/articles";
import useArticles from "../hooks/useArticles";

type Inputs = {
    order: OrderInput[];
};

type KronologizeButtonProps = {
    timelineId: number;
};

export default function KronologizeButton({
    timelineId,
}: KronologizeButtonProps) {
    const { user, loading, loggedOut, mutate } = useCurrentUser();
    const userId = user && user.id;
    const { articles, mutate: mutateArticles } = useArticles(
        userId,
        timelineId ? timelineId.toString() : undefined
    );

    const {
        register,
        handleSubmit,
        watch,
        setValue,
        reset,
        formState: { errors },
        control,
    } = useForm<Inputs>({
        defaultValues: {
            order:
                articles?.map((it) => ({
                    id: it.id.toString(),
                    title: it.title,
                })) || [],
        },
    });

    const [showArticleForm, setShowArticleForm] = useState(false);
    const ref = React.useRef();
    const onSubmit: SubmitHandler<Inputs> = async (data) => {
        const formattedData = {
            order: data.order.map((it) => parseInt(it.id)),
        };
        const response = await fetch(
            `http://localhost:7070/api/timelines/${timelineId}/order`,
            {
                method: "PUT",
                credentials: "include",
                body: JSON.stringify(formattedData),
            }
        );

        refreshArticles();
        // setShowArticleForm(false);
        ref.current?.click();
        reset();
    };

    return (
        <Dialog>
            <DialogTrigger asChild ref={ref}>
                {timelineId && (
                    <button
                        className="button"
                        onClick={() => {
                            setShowArticleForm(true);
                            mutateArticles();
                        }}
                    >
                        EDIT TIMELINE
                    </button>
                )}
            </DialogTrigger>
            <DialogContent className="sm:max-w-[90vw]">
                <form onSubmit={handleSubmit(onSubmit)}>
                    <DialogHeader>
                        <DialogTitle>EDIT TIMELINE</DialogTitle>
                        <DialogDescription>
                            Reorder the presentation of articles for a timeline
                            so that they are shown chronologically.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="flex flex-col gap-4 py-4">
                        <label className="flex flex-col" htmlFor="">
                            Article Order
                            <Controller
                                name="order"
                                control={control}
                                render={({ field }) => (
                                    <KronologizeInput
                                        id="order"
                                        setOrder={(
                                            orderInput: OrderInput[]
                                        ) => {
                                            setValue("order", orderInput);
                                        }}
                                        order={field.value}
                                    />
                                )}
                            ></Controller>
                        </label>
                    </div>
                    <DialogFooter>
                        <button className="button" type="submit">
                            Save Changes
                        </button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
